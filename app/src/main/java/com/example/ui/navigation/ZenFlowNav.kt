package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FlowEntity
import com.example.data.JsonUtils
import com.example.ui.components.AiRoutineDialog
import com.example.ui.components.CreateRoutineDialog
import com.example.ui.components.EnvironmentDrawer
import com.example.ui.components.FeatureGuideDialog
import com.example.ui.components.OnboardingDialog
import com.example.ui.components.QuickToast
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MyFlowsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ZeroLogicEditorScreen
import com.example.viewmodel.AiRoutineViewModel
import com.example.viewmodel.ZenFlowViewModel

import androidx.compose.material.icons.filled.BarChart
import com.example.ui.screens.DashboardScreen

enum class ZenFlowTab(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    MY_FLOWS("my_flows", "My Flows", Icons.Default.AutoMode),
    DASHBOARD("dashboard", "Dashboard", Icons.Default.BarChart),
    DISCOVER("discover", "Discover", Icons.Default.Explore),
    HISTORY("history", "History", Icons.Default.History),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenFlowNav(
    viewModel: ZenFlowViewModel,
    modifier: Modifier = Modifier
) {
    val aiViewModel: AiRoutineViewModel = viewModel()
    var isAiDialogOpen by remember { mutableStateOf(false) }
    var isCreateRoutineDialogOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(ZenFlowTab.MY_FLOWS) }
    var isEditingFlow by remember { mutableStateOf(false) }
    var editingFlowTarget by remember { mutableStateOf<FlowEntity?>(null) }
    var isGuideDialogOpen by remember { mutableStateOf(false) }

    val userFlows by viewModel.filteredUserFlows.collectAsStateWithLifecycle()
    val templates by viewModel.filteredTemplates.collectAsStateWithLifecycle()
    val historyLogs by viewModel.historyLogs.collectAsStateWithLifecycle()
    val executingFlowIds by viewModel.executingFlowIds.collectAsStateWithLifecycle()
    val environment by viewModel.environment.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val autoPruneLogsEnabled by viewModel.autoPruneLogsEnabled.collectAsStateWithLifecycle()
    val batterySaverMode by viewModel.batterySaverMode.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isSimulatingDrawerOpen by viewModel.isSimulatingDrawerOpen.collectAsStateWithLifecycle()
    val smartSuggestionsEnabled by viewModel.smartSuggestionsEnabled.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

    var isOnboardingOpen by remember(hasCompletedOnboarding) { mutableStateOf(!hasCompletedOnboarding) }

    val undoEvent by viewModel.undoSnackbarEvent.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(undoEvent) {
        undoEvent?.let { event ->
            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = "Undo",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.triggerUndo(event)
            } else {
                viewModel.clearUndoSnackbar()
            }
        }
    }

    if (isEditingFlow) {
        ZeroLogicEditorScreen(
            editingFlow = editingFlowTarget,
            environment = environment,
            onSaveFlow = { id, title, desc, cat, icon, color, triggerType, triggerCfg, actionCfg, scheduledTime ->
                viewModel.saveFlow(id, title, desc, cat, icon, color, triggerType, triggerCfg, actionCfg, scheduledTime)
                isEditingFlow = false
                editingFlowTarget = null
            },
            onBack = {
                isEditingFlow = false
                editingFlowTarget = null
            }
        )
    } else {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val isTabletOrFoldable = screenWidth >= 600.dp

        Scaffold(
            snackbarHost = {
                androidx.compose.material3.SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.testTag("undo_snackbar_host")
                )
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                ZenFlowTab.MY_FLOWS -> "SwAIft"
                                ZenFlowTab.DASHBOARD -> "Execution Analytics"
                                ZenFlowTab.DISCOVER -> "Routine Gallery"
                                ZenFlowTab.HISTORY -> "Transparency Log"
                                ZenFlowTab.SETTINGS -> "Settings & Privacy"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        Row {
                            IconButton(onClick = { isGuideDialogOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Feature Guidelines",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { viewModel.toggleSimulatingDrawer() }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Simulate Environment",
                                    tint = if (isSimulatingDrawerOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isTabletOrFoldable) {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        ZenFlowTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                label = { Text(text = tab.title) },
                                modifier = Modifier.testTag("nav_item_${tab.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTabletOrFoldable) {
                    androidx.compose.material3.NavigationRail(
                        modifier = Modifier
                            .testTag("navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ZenFlowTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            androidx.compose.material3.NavigationRailItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                                label = { Text(text = tab.title, fontSize = 11.sp) },
                                modifier = Modifier.testTag("rail_item_${tab.route}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                when (selectedTab) {
                    ZenFlowTab.MY_FLOWS -> {
                        MyFlowsScreen(
                            flows = userFlows,
                            historyLogs = historyLogs,
                            executingFlowIds = executingFlowIds,
                            environment = environment,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            recentSearches = recentSearches,
                            onAddRecentSearch = { viewModel.addRecentSearch(it) },
                            onCategorySelected = { viewModel.setCategory(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onToggleFlow = { viewModel.toggleFlowEnabled(it) },
                            onRunFlowNow = { viewModel.runFlowManually(it) },
                            onDeleteFlow = { viewModel.deleteFlow(it) },
                            onBatchDeleteFlows = { viewModel.batchDeleteFlows(it) },
                            onBatchUpdateEnabled = { ids, enable -> viewModel.batchUpdateEnabled(ids, enable) },
                            onEditFlow = { flow ->
                                editingFlowTarget = flow
                                isEditingFlow = true
                            },
                            onCreateNewFlow = {
                                isCreateRoutineDialogOpen = true
                            },
                            onNavigateToDiscover = { selectedTab = ZenFlowTab.DISCOVER },
                            onOpenEnvironmentSimulator = { viewModel.toggleSimulatingDrawer() },
                            onOpenAiDialog = { isAiDialogOpen = true },
                            isLoading = isLoading
                        )
                    }
                    ZenFlowTab.DASHBOARD -> {
                        DashboardScreen(
                            userFlows = userFlows,
                            historyLogs = historyLogs,
                            aiViewModel = aiViewModel
                        )
                    }
                    ZenFlowTab.DISCOVER -> {
                        DiscoverScreen(
                            templates = templates,
                            userFlows = userFlows,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            recentSearches = recentSearches,
                            onAddRecentSearch = { viewModel.addRecentSearch(it) },
                            onCategorySelected = { viewModel.setCategory(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onToggleTemplate = { template, enable ->
                                viewModel.toggleTemplate(template, enable)
                            },
                            onRunTemplate = { template ->
                                viewModel.runTemplateManually(template)
                            },
                            onEditTemplate = { template ->
                                editingFlowTarget = template
                                isEditingFlow = true
                            }
                        )
                    }
                    ZenFlowTab.HISTORY -> {
                        HistoryScreen(
                            logs = historyLogs,
                            onClearLogs = { viewModel.clearLogs() }
                        )
                    }
                    ZenFlowTab.SETTINGS -> {
                        val apiErrorStatus by aiViewModel.apiErrorStatus.collectAsStateWithLifecycle()
                        SettingsScreen(
                            themeMode = themeMode,
                            onSetThemeMode = { viewModel.setThemeMode(it) },
                            autoPruneLogsEnabled = autoPruneLogsEnabled,
                            onSetAutoPruneLogs = { viewModel.setAutoPruneLogsEnabled(it) },
                            batterySaverModeEnabled = batterySaverMode,
                            onSetBatterySaverMode = { viewModel.setBatterySaverMode(it) },
                            onExportBackupJson = { viewModel.exportDatabaseBackupJson() },
                            onImportBackupJson = { viewModel.importDatabaseBackupJson(it) },
                            onShowToast = { viewModel.showToast(it) },
                            customApiKey = aiViewModel.getCustomApiKey(),
                            onSaveCustomApiKey = { aiViewModel.saveCustomApiKey(it) },
                            apiErrorStatus = apiErrorStatus,
                            onClearApiError = { aiViewModel.clearApiErrorStatus() },
                            smartSuggestionsEnabled = smartSuggestionsEnabled,
                            onSetSmartSuggestions = { viewModel.setSmartSuggestionsEnabled(it) },
                            onReplayOnboarding = { isOnboardingOpen = true },
                            onOpenFeatureGuide = { isGuideDialogOpen = true }
                        )
                    }
                }

                // Light-touch Toast Floating Notification Overlay
                QuickToast(
                    message = toastMessage,
                    onDismiss = { viewModel.clearToast() }
                )
            }
        }
    }
}
}

    // Environment Simulator Bottom Drawer
    if (isSimulatingDrawerOpen) {
        EnvironmentDrawer(
            environment = environment,
            onDismiss = { viewModel.toggleSimulatingDrawer() },
            onUpdateLocation = { viewModel.updateLocation(it) },
            onUpdateWifi = { viewModel.updateWifi(it) },
            onUpdateBluetooth = { viewModel.updateBluetooth(it) },
            onUpdateBattery = { pct, chg -> viewModel.updateBattery(pct, chg) },
            onUpdateApp = { viewModel.updateApp(it) },
            onUpdateActivity = { viewModel.updateActivity(it) },
            onUpdateTime = { viewModel.updateTime(it) }
        )
    }

    // Create Routine Form Dialog
    if (isCreateRoutineDialogOpen) {
        CreateRoutineDialog(
            onDismiss = { isCreateRoutineDialogOpen = false },
            onSaveRoutine = { title, desc, cat, icon, color, triggerType, scheduledTime, triggerCfg, actionCfg, tags ->
                viewModel.saveFlow(
                    id = null,
                    title = title,
                    description = desc,
                    category = cat,
                    iconName = icon,
                    colorHex = color,
                    triggerType = triggerType,
                    triggerConfig = triggerCfg,
                    actionConfig = actionCfg,
                    scheduledTime = scheduledTime,
                    tags = tags
                )
            }
        )
    }

    // AI Routine Natural Language Dialog
    if (isAiDialogOpen) {
        AiRoutineDialog(
            aiViewModel = aiViewModel,
            userFlows = userFlows,
            onDismiss = { isAiDialogOpen = false },
            onSaveGeneratedFlow = { generatedFlow ->
                val triggerCfg = JsonUtils.deserializeTrigger(generatedFlow.triggerConfigJson)
                val actionCfg = JsonUtils.deserializeAction(generatedFlow.actionConfigJson)
                viewModel.saveFlow(
                    id = generatedFlow.id,
                    title = generatedFlow.title,
                    description = generatedFlow.description,
                    category = generatedFlow.category,
                    iconName = generatedFlow.iconName,
                    colorHex = generatedFlow.colorHex,
                    triggerType = generatedFlow.triggerType,
                    triggerConfig = triggerCfg,
                    actionConfig = actionCfg
                )
                viewModel.showToast("✓ Saved AI Routine '${generatedFlow.title}'")
            }
        )
    }

    if (isOnboardingOpen) {
        OnboardingDialog(
            onDismiss = {
                isOnboardingOpen = false
                viewModel.setOnboardingCompleted(true)
            },
            onComplete = {
                isOnboardingOpen = false
                viewModel.setOnboardingCompleted(true)
            }
        )
    }

    if (isGuideDialogOpen) {
        FeatureGuideDialog(
            onDismiss = { isGuideDialogOpen = false }
        )
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val ratingManager = remember { com.example.engine.SmartAppRatingManager(context) }
    var activeSmartPrompt by remember { mutableStateOf<com.example.ui.components.SmartPromptType?>(null) }

    androidx.compose.runtime.LaunchedEffect(historyLogs.size) {
        if (activeSmartPrompt == null && historyLogs.isNotEmpty()) {
            activeSmartPrompt = ratingManager.shouldShowEngagementPrompt()
        }
    }

    activeSmartPrompt?.let { promptType ->
        com.example.ui.components.SmartRatingDialog(
            promptType = promptType,
            onDismiss = { activeSmartPrompt = null },
            onRateClicked = {
                ratingManager.markRated()
                activeSmartPrompt = null
            },
            onShareClicked = {
                ratingManager.markShared()
                activeSmartPrompt = null
            }
        )
    }
}

