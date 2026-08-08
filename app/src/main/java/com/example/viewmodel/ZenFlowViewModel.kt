package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.service.AlarmScheduler
import com.example.data.ActionConfig
import com.example.data.FlowEntity
import com.example.data.FlowRepository
import com.example.data.HistoryLogEntity
import com.example.data.JsonUtils
import com.example.data.TriggerConfig
import com.example.data.TriggerType
import com.example.data.ZenFlowDatabase
import com.example.engine.EnvironmentSimulator
import com.example.engine.FlowExecutor
import com.example.engine.SimulatedEnvironment
import com.example.util.MainThreadWatchdog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ZenFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ZenFlowDatabase.getDatabase(application, viewModelScope)
    private val repository = FlowRepository(database.flowDao(), database.historyLogDao())
    private val watchdog = MainThreadWatchdog(database.historyLogDao())


    private val prefs = application.getSharedPreferences("swaift_vm_prefs", android.content.Context.MODE_PRIVATE)

    private val _executingFlowIds = MutableStateFlow<Set<String>>(emptySet())
    val executingFlowIds: StateFlow<Set<String>> = _executingFlowIds.asStateFlow()

    val userFlows = repository.userFlows
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates = repository.templates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyLogs = repository.historyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val environment: StateFlow<SimulatedEnvironment> = EnvironmentSimulator.environment

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode = _themeMode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _autoPruneLogsEnabled = MutableStateFlow(prefs.getBoolean("auto_prune_logs", true))
    val autoPruneLogsEnabled = _autoPruneLogsEnabled.asStateFlow()

    private val _batterySaverMode = MutableStateFlow(prefs.getBoolean("battery_saver_mode", false))
    val batterySaverMode = _batterySaverMode.asStateFlow()

    fun setBatterySaverMode(enabled: Boolean) {
        _batterySaverMode.value = enabled
        prefs.edit().putBoolean("battery_saver_mode", enabled).apply()
        showToast(if (enabled) "Battery Saver Mode enabled (polling slowed)" else "Battery Saver Mode disabled")
    }

    private val _smartSuggestionsEnabled = MutableStateFlow(prefs.getBoolean("smart_suggestions", true))
    val smartSuggestionsEnabled = _smartSuggestionsEnabled.asStateFlow()

    fun setSmartSuggestionsEnabled(enabled: Boolean) {
        _smartSuggestionsEnabled.value = enabled
        prefs.edit().putBoolean("smart_suggestions", enabled).apply()
        showToast(if (enabled) "Smart Suggestions Enabled" else "Smart Suggestions Disabled")
    }

    init {
        watchdog.start()
        pruneOldLogsNow()
        // Template pre-population is handled by ZenFlowDatabaseCallback.onCreate();
        // no need to duplicate it here.
        viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        watchdog.stop()
    }

    fun onClearedForTest() {
        watchdog.stop()
    }

    fun pruneOldLogsNow() {
        if (_autoPruneLogsEnabled.value) {
            viewModelScope.launch {
                val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000L)
                repository.pruneLogsOlderThan(ninetyDaysAgo)
            }
        }
    }

    fun setAutoPruneLogsEnabled(enabled: Boolean) {
        _autoPruneLogsEnabled.value = enabled
        prefs.edit().putBoolean("auto_prune_logs", enabled).apply()
        if (enabled) {
            pruneOldLogsNow()
            showToast("Log auto-pruning (90 days) enabled")
        } else {
            showToast("Log auto-pruning disabled")
        }
    }

    private val _recentSearches = MutableStateFlow(listOf("Morning", "WiFi", "Focus", "Battery"))
    val recentSearches = _recentSearches.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
        val label = when (mode) {
            "LIGHT" -> "Light Mode"
            "DARK" -> "Dark Mode"
            else -> "System Default"
        }
        showToast("Theme updated to $label")
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        _recentSearches.value = current.take(5)
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun exportDatabaseBackupJson(): String {
        val flows = userFlows.value
        val logs = historyLogs.value
        return JsonUtils.serializeBackup(flows, logs)
    }

    fun importDatabaseBackupJson(jsonStr: String) {
        viewModelScope.launch {
            try {
                val (flows, logs) = JsonUtils.parseBackup(jsonStr)
                flows.forEach { repository.insertFlow(it) }
                logs.forEach { repository.addLog(it) }
                showToast("Restored ${flows.size} routine(s) and ${logs.size} log(s) from backup!")
            } catch (e: Exception) {
                showToast("Import failed: Invalid backup format")
            }
        }
    }

    private val _isSimulatingDrawerOpen = MutableStateFlow(false)
    val isSimulatingDrawerOpen = _isSimulatingDrawerOpen.asStateFlow()

    // Filtered User Flows
    val filteredUserFlows: StateFlow<List<FlowEntity>> = combine(
        userFlows,
        selectedCategory,
        searchQuery
    ) { flows, category, query ->
        flows.filter { flow ->
            val matchesCategory = (category == "All" || flow.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    flow.title.contains(query, ignoreCase = true) ||
                    flow.description.contains(query, ignoreCase = true) ||
                    flow.category.contains(query, ignoreCase = true) ||
                    flow.triggerType.displayName.contains(query, ignoreCase = true) ||
                    flow.triggerConfigJson.contains(query, ignoreCase = true) ||
                    flow.actionConfigJson.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Templates
    val filteredTemplates: StateFlow<List<FlowEntity>> = combine(
        templates,
        selectedCategory,
        searchQuery
    ) { tpls, category, query ->
        tpls.filter { tpl ->
            val matchesCategory = (category == "All" || tpl.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    tpl.title.contains(query, ignoreCase = true) ||
                    tpl.description.contains(query, ignoreCase = true) ||
                    tpl.triggerType.displayName.contains(query, ignoreCase = true) ||
                    tpl.triggerConfigJson.contains(query, ignoreCase = true) ||
                    tpl.actionConfigJson.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSimulatingDrawer() {
        _isSimulatingDrawerOpen.value = !_isSimulatingDrawerOpen.value
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    private var lastDeletedFlows: List<FlowEntity> = emptyList()
    private var lastPausedFlows: List<FlowEntity> = emptyList()

    enum class UndoType { DELETE, PAUSE }
    data class UndoSnackbarEvent(val id: Long = System.currentTimeMillis(), val message: String, val type: UndoType)

    private val _undoSnackbarEvent = MutableStateFlow<UndoSnackbarEvent?>(null)
    val undoSnackbarEvent = _undoSnackbarEvent.asStateFlow()

    fun clearUndoSnackbar() {
        _undoSnackbarEvent.value = null
    }

    fun triggerUndo(event: UndoSnackbarEvent) {
        viewModelScope.launch {
            when (event.type) {
                UndoType.DELETE -> {
                    lastDeletedFlows.forEach { flow ->
                        repository.insertFlow(flow)
                    }
                    showToast("Restored ${lastDeletedFlows.size} routine(s)")
                    lastDeletedFlows = emptyList()
                }
                UndoType.PAUSE -> {
                    lastPausedFlows.forEach { flow ->
                        repository.updateFlow(flow.copy(isEnabled = true))
                    }
                    showToast("Resumed ${lastPausedFlows.size} routine(s)")
                    lastPausedFlows = emptyList()
                }
            }
            clearUndoSnackbar()
        }
    }

    fun toggleFlowEnabled(flow: FlowEntity) {
        viewModelScope.launch {
            val updated = flow.copy(isEnabled = !flow.isEnabled)
            repository.updateFlow(updated)
            updateAlarmScheduling(updated)
            val stateText = if (updated.isEnabled) "Activated" else "Deactivated"
            showToast("Flow '${flow.title}' $stateText")

            if (flow.isEnabled) {
                // Was enabled, now paused
                lastPausedFlows = listOf(flow)
                _undoSnackbarEvent.value = UndoSnackbarEvent(
                    message = "Paused routine '${flow.title}'",
                    type = UndoType.PAUSE
                )
            }
        }
    }

    fun runFlowManually(flow: FlowEntity) {
        viewModelScope.launch {
            _executingFlowIds.value = _executingFlowIds.value + flow.id
            try {
                delay(600) // Brief delay to show Execution Pulse animation
                val result = FlowExecutor.executeFlow(flow, reasonOverride = "Manual test run by user")
                
                val isBatterySaverActive = _batterySaverMode.value
                val isFailure = isBatterySaverActive && (flow.category == "Battery" || flow.category == "Connectivity")

                if (isFailure) {
                    repository.addLog(
                        HistoryLogEntity(
                            flowId = flow.id,
                            flowTitle = flow.title,
                            iconName = flow.iconName,
                            colorHex = flow.colorHex,
                            status = "FAILED",
                            triggerReason = result.triggerReason,
                            actionsExecuted = "Failed: Execution restricted by Battery Saver Mode"
                        )
                    )
                    showToast("❌ Manual execution restricted by Battery Saver Mode!")
                } else {
                    val updatedFlow = flow.copy(
                        lastRunTimeMillis = System.currentTimeMillis(),
                        runCount = flow.runCount + 1
                    )
                    repository.updateFlow(updatedFlow)

                    repository.addLog(
                        HistoryLogEntity(
                            flowId = flow.id,
                            flowTitle = flow.title,
                            iconName = flow.iconName,
                            colorHex = flow.colorHex,
                            status = "MANUAL",
                            triggerReason = result.triggerReason,
                            actionsExecuted = result.actionsSummary
                        )
                    )

                    showToast("▶ Flow '${flow.title}' executed! ${result.actionsSummary}")
                }
            } finally {
                _executingFlowIds.value = _executingFlowIds.value - flow.id
            }
        }
    }

    fun activateTemplate(template: FlowEntity) {
        viewModelScope.launch {
            val existing = userFlows.value.find { it.title.equals(template.title, ignoreCase = true) }
            if (existing != null) {
                showToast("⚠️ Conflict: Routine '${template.title}' already exists in your active list.")
                return@launch
            }
            if (template.title.isBlank() || template.actionConfigJson.isBlank()) {
                showToast("❌ Missing requirements: Template configuration is incomplete.")
                return@launch
            }
            val newFlow = template.copy(
                id = UUID.randomUUID().toString(),
                isEnabled = true,
                isTemplate = false,
                lastRunTimeMillis = null,
                runCount = 0
            )
            repository.insertFlow(newFlow)
            showToast("✓ Template validated & activated '${template.title}'!")
        }
    }

    fun toggleTemplate(template: FlowEntity, enable: Boolean) {
        viewModelScope.launch {
            val existing = userFlows.value.find { it.title.equals(template.title, ignoreCase = true) }
            if (enable) {
                if (existing != null) {
                    repository.updateFlow(existing.copy(isEnabled = true))
                    showToast("Enabled routine '${template.title}'")
                } else {
                    val newFlow = template.copy(
                        id = UUID.randomUUID().toString(),
                        isEnabled = true,
                        isTemplate = false,
                        lastRunTimeMillis = null,
                        runCount = 0
                    )
                    repository.insertFlow(newFlow)
                    showToast("✓ Activated pre-built routine '${template.title}'!")
                }
            } else {
                if (existing != null) {
                    repository.updateFlow(existing.copy(isEnabled = false))
                    showToast("Disabled routine '${template.title}'")
                }
            }
        }
    }

    fun saveFlow(
        id: String?,
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        triggerType: TriggerType,
        triggerConfig: TriggerConfig,
        actionConfig: ActionConfig,
        scheduledTime: String = "08:00",
        tags: String = "General"
    ) {
        viewModelScope.launch {
            val flowId = id ?: UUID.randomUUID().toString()
            val flow = FlowEntity(
                id = flowId,
                title = title.ifBlank { "My Automation Flow" },
                description = description.ifBlank { "Custom automated flow" },
                category = category,
                iconName = iconName,
                colorHex = colorHex,
                isEnabled = true,
                isTemplate = false,
                triggerType = triggerType,
                triggerConfigJson = JsonUtils.serializeTrigger(triggerConfig),
                actionConfigJson = JsonUtils.serializeAction(actionConfig),
                scheduledTime = scheduledTime,
                tags = tags
            )
            repository.insertFlow(flow)
            updateAlarmScheduling(flow)
            showToast("✓ Saved routine '${flow.title}'")
        }
    }

    fun deleteFlow(flow: FlowEntity) {
        viewModelScope.launch {
            lastDeletedFlows = listOf(flow)
            repository.deleteFlow(flow)
            AlarmScheduler.cancelAlarm(getApplication(), flow)
            showToast("Deleted flow '${flow.title}'")
            _undoSnackbarEvent.value = UndoSnackbarEvent(
                message = "Deleted routine '${flow.title}'",
                type = UndoType.DELETE
            )
        }
    }

    fun batchDeleteFlows(flowIds: Set<String>) {
        viewModelScope.launch {
            val toDelete = userFlows.value.filter { it.id in flowIds }
            lastDeletedFlows = toDelete
            toDelete.forEach { flow ->
                AlarmScheduler.cancelAlarm(getApplication(), flow)
            }
            flowIds.forEach { id ->
                repository.deleteFlowById(id)
            }
            showToast("Deleted ${flowIds.size} routines")
            _undoSnackbarEvent.value = UndoSnackbarEvent(
                message = "Deleted ${toDelete.size} routine(s)",
                type = UndoType.DELETE
            )
        }
    }

    fun batchUpdateEnabled(flowIds: Set<String>, enable: Boolean) {
        viewModelScope.launch {
            val targetFlows = userFlows.value.filter { it.id in flowIds }
            targetFlows.forEach { flow ->
                val updated = flow.copy(isEnabled = enable)
                repository.updateFlow(updated)
                updateAlarmScheduling(updated)
            }
            val statusLabel = if (enable) "Resumed" else "Paused"
            showToast("$statusLabel ${flowIds.size} routines")

            if (!enable) {
                lastPausedFlows = targetFlows
                _undoSnackbarEvent.value = UndoSnackbarEvent(
                    message = "Paused ${targetFlows.size} routine(s)",
                    type = UndoType.PAUSE
                )
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearHistoryLogs()
            showToast("Cleared history log")
        }
    }

    // Environmental Simulation Triggers
    fun updateLocation(label: String) {
        EnvironmentSimulator.updateLocation(label)
        evaluateEnvironmentTriggers("Location changed to '$label'")
    }

    fun updateWifi(ssid: String) {
        EnvironmentSimulator.updateWifi(ssid)
        evaluateEnvironmentTriggers("Wi-Fi connected to '$ssid'")
    }

    fun updateBluetooth(device: String) {
        EnvironmentSimulator.updateBluetooth(device)
        evaluateEnvironmentTriggers("Bluetooth connected to '$device'")
    }

    fun updateBattery(percent: Int, isCharging: Boolean) {
        EnvironmentSimulator.updateBattery(percent, isCharging)
        val status = if (isCharging) "Charging ($percent%)" else "Battery at $percent%"
        evaluateEnvironmentTriggers(status)
    }

    fun updateApp(appName: String) {
        EnvironmentSimulator.updateApp(appName)
        evaluateEnvironmentTriggers("Opened app '$appName'")
    }

    fun updateActivity(activity: String) {
        EnvironmentSimulator.updateActivity(activity)
        evaluateEnvironmentTriggers("Detected activity '$activity'")
    }

    fun updateTime(timeStr: String) {
        EnvironmentSimulator.updateTime(timeStr)
        evaluateEnvironmentTriggers("Time reached '$timeStr'")
    }

    private fun evaluateEnvironmentTriggers(envChangeDescription: String) {
        viewModelScope.launch {
            val currentEnv = EnvironmentSimulator.environment.value
            val activeFlowsList = userFlows.first().filter { it.isEnabled }
 
            var triggeredCount = 0
            var failedCount = 0
            activeFlowsList.forEach { flow ->
                if (FlowExecutor.isTriggerMatched(flow, currentEnv)) {
                    _executingFlowIds.value = _executingFlowIds.value + flow.id
                    try {
                        delay(500)
                        val result = FlowExecutor.executeFlow(flow)
                        
                        val isBatterySaverActive = _batterySaverMode.value
                        val isFailure = isBatterySaverActive && (flow.category == "Battery" || flow.category == "Connectivity")

                        if (isFailure) {
                            repository.addLog(
                                HistoryLogEntity(
                                    flowId = flow.id,
                                    flowTitle = flow.title,
                                    iconName = flow.iconName,
                                    colorHex = flow.colorHex,
                                    status = "FAILED",
                                    triggerReason = "$envChangeDescription → ${result.triggerReason}",
                                    actionsExecuted = "Failed: Restricted by Battery Saver Mode"
                                )
                            )
                            failedCount++
                        } else {
                            val updatedFlow = flow.copy(
                                lastRunTimeMillis = System.currentTimeMillis(),
                                runCount = flow.runCount + 1
                            )
                            repository.updateFlow(updatedFlow)
 
                            repository.addLog(
                                HistoryLogEntity(
                                    flowId = flow.id,
                                    flowTitle = flow.title,
                                    iconName = flow.iconName,
                                    colorHex = flow.colorHex,
                                    status = "SUCCESS",
                                    triggerReason = "$envChangeDescription → ${result.triggerReason}",
                                    actionsExecuted = result.actionsSummary
                                )
                            )
                            triggeredCount++
                        }
                    } finally {
                        _executingFlowIds.value = _executingFlowIds.value - flow.id
                    }
                }
            }
 
            if (triggeredCount > 0) {
                showToast("⚡ SwAIft auto-triggered $triggeredCount flow(s)!")
            }
            if (failedCount > 0) {
                showToast("⚠️ $failedCount flow(s) failed due to Battery Saver limits")
            }
        }
    }

    private fun updateAlarmScheduling(flow: FlowEntity) {
        val trigger = JsonUtils.deserializeTrigger(flow.triggerConfigJson)
        if (flow.isEnabled && flow.triggerType == TriggerType.SCHEDULE && trigger.useAlarmManager) {
            AlarmScheduler.scheduleAlarm(getApplication(), flow)
        } else {
            AlarmScheduler.cancelAlarm(getApplication(), flow)
        }
    }
}
