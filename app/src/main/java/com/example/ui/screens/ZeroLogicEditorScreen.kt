package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ActionConfig
import com.example.data.FlowEntity
import com.example.data.JsonUtils
import com.example.data.TriggerConfig
import com.example.data.TriggerType
import com.example.engine.SimulatedEnvironment
import com.example.ui.components.getFlowIconVector

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ZeroLogicEditorScreen(
    editingFlow: FlowEntity?,
    environment: SimulatedEnvironment,
    onSaveFlow: (
        id: String?,
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        triggerType: TriggerType,
        triggerConfig: TriggerConfig,
        actionConfig: ActionConfig,
        scheduledTime: String
    ) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialTrigger = editingFlow?.let { JsonUtils.deserializeTrigger(it.triggerConfigJson) } ?: TriggerConfig(
        locationLabel = environment.locationLabel,
        wifiSsid = environment.wifiSsid,
        batteryThreshold = 20
    )
    val initialAction = editingFlow?.let { JsonUtils.deserializeAction(it.actionConfigJson) } ?: ActionConfig(
        ringtoneMode = "SILENT",
        enableDnd = true,
        setBrightness = true,
        brightnessPercent = 20
    )

    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Intention, Step 2: Condition, Step 3: Name & Confirm

    // Flow metadata state
    var title by remember { mutableStateOf(editingFlow?.title ?: "") }
    var description by remember { mutableStateOf(editingFlow?.description ?: "") }
    var category by remember { mutableStateOf(editingFlow?.category ?: "Lifestyle") }
    var iconName by remember { mutableStateOf(editingFlow?.iconName ?: "Bedtime") }
    var colorHex by remember { mutableStateOf(editingFlow?.colorHex ?: "#818CF8") }

    // Trigger State
    var triggerType by remember { mutableStateOf(editingFlow?.triggerType ?: TriggerType.SCHEDULE) }
    var locationLabel by remember { mutableStateOf(initialTrigger.locationLabel) }
    var timeStart by remember { mutableStateOf(initialTrigger.timeStart) }
    var timeEnd by remember { mutableStateOf(initialTrigger.timeEnd) }
    var wifiSsid by remember { mutableStateOf(initialTrigger.wifiSsid) }
    var bluetoothDevice by remember { mutableStateOf(initialTrigger.bluetoothDeviceName) }
    var batteryThreshold by remember { mutableIntStateOf(initialTrigger.batteryThreshold) }
    var batteryIsCharging by remember { mutableStateOf(initialTrigger.batteryIsCharging) }
    var appName by remember { mutableStateOf(initialTrigger.appName) }
    var activityType by remember { mutableStateOf(initialTrigger.activityType) }

    // Action State
    var ringtoneMode by remember { mutableStateOf(initialAction.ringtoneMode) }
    var setMediaVolume by remember { mutableStateOf(initialAction.setMediaVolume) }
    var mediaVolumePercent by remember { mutableIntStateOf(initialAction.mediaVolumePercent) }
    var enableDnd by remember { mutableStateOf(initialAction.enableDnd) }
    var setBrightness by remember { mutableStateOf(initialAction.setBrightness) }
    var brightnessPercent by remember { mutableIntStateOf(initialAction.brightnessPercent) }
    var enableDarkMode by remember { mutableStateOf(initialAction.enableDarkMode) }
    var toggleWifi by remember { mutableStateOf(initialAction.toggleWifi) }
    var wifiState by remember { mutableStateOf(initialAction.wifiState) }
    var toggleBluetooth by remember { mutableStateOf(initialAction.toggleBluetooth) }
    var bluetoothState by remember { mutableStateOf(initialAction.bluetoothState) }
    var enableAutoRotate by remember { mutableStateOf(initialAction.enableAutoRotate) }
    var announceTts by remember { mutableStateOf(initialAction.announceTts) }
    var ttsMessage by remember { mutableStateOf(initialAction.ttsMessage) }
    var delayedSeconds by remember { mutableIntStateOf(initialAction.delayedSeconds) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingFlow == null) "Zero-Logic Q&A Setup" else "Edit Flow",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("1. Goal", "2. When/Where", "3. Save").forEachIndexed { idx, label ->
                    val stepNum = idx + 1
                    val isActive = currentStep == stepNum
                    val isDone = currentStep > stepNum

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { currentStep = stepNum }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isActive -> MaterialTheme.colorScheme.primary
                                        isDone -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "$stepNum",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (currentStep) {
                // STEP 1: WHAT DO YOU WANT TO ACHIEVE? (INTENTION / ACTIONS)
                1 -> {
                    Text(
                        text = "Step 1: What do you want your phone to do?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Select one or more actions to automate effortlessly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Ringer & Audio Mode
                    Text("🔔 Ringer & Sound Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NORMAL", "VIBRATE", "SILENT").forEach { mode ->
                            FilterChip(
                                selected = ringtoneMode == mode,
                                onClick = { ringtoneMode = mode },
                                label = { Text(mode) },
                                modifier = Modifier.testTag("mode_$mode")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔊 Set Media Volume", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = setMediaVolume,
                            onCheckedChange = { setMediaVolume = it },
                            modifier = Modifier.testTag("toggle_media_vol")
                        )
                    }
                    if (setMediaVolume) {
                        Text("$mediaVolumePercent%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Slider(
                            value = mediaVolumePercent.toFloat(),
                            onValueChange = { mediaVolumePercent = it.toInt() },
                            valueRange = 0f..100f
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Do Not Disturb
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌙 Do Not Disturb (DND)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = enableDnd,
                            onCheckedChange = { enableDnd = it },
                            modifier = Modifier.testTag("toggle_dnd")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Display Brightness & Dark Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("☀️ Adjust Screen Brightness", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = setBrightness,
                            onCheckedChange = { setBrightness = it },
                            modifier = Modifier.testTag("toggle_brightness")
                        )
                    }
                    if (setBrightness) {
                        Text("$brightnessPercent%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Slider(
                            value = brightnessPercent.toFloat(),
                            onValueChange = { brightnessPercent = it.toInt() },
                            valueRange = 5f..100f
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌓 Dark Mode Theme", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = enableDarkMode,
                            onCheckedChange = { enableDarkMode = it },
                            modifier = Modifier.testTag("toggle_dark_mode")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wi-Fi / Bluetooth Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📶 Toggle Wi-Fi State", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (toggleWifi) {
                                Text(if (wifiState) "Turn ON" else "Turn OFF", style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = toggleWifi,
                                onCheckedChange = { toggleWifi = it },
                                modifier = Modifier.testTag("toggle_wifi")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Smart TTS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗣️ Text-To-Speech Audio Announcement", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = announceTts,
                            onCheckedChange = { announceTts = it },
                            modifier = Modifier.testTag("toggle_tts")
                        )
                    }
                    if (announceTts) {
                        OutlinedTextField(
                            value = ttsMessage,
                            onValueChange = { ttsMessage = it },
                            label = { Text("Speech Announcement Message") },
                            modifier = Modifier.fillMaxWidth().testTag("tts_msg_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { currentStep = 2 },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("step1_next_btn")
                    ) {
                        Text("Next: Choose When or Where →")
                    }
                }

                // STEP 2: WHEN OR WHERE SHOULD THIS HAPPEN? (CONDITIONS / TRIGGERS)
                2 -> {
                    Text(
                        text = "Step 2: When or Where should this run?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Select the trigger condition that activates your flow.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Trigger Type Selector Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TriggerType.entries.forEach { type ->
                            FilterChip(
                                selected = triggerType == type,
                                onClick = { triggerType = type },
                                label = { Text(type.displayName) },
                                modifier = Modifier.testTag("trigger_chip_${type.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when (triggerType) {
                                TriggerType.LOCATION -> {
                                    Text("📍 GPS Geofence Location", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = locationLabel,
                                        onValueChange = { locationLabel = it },
                                        label = { Text("Location Name (e.g., Home, Office, Gym)") },
                                        modifier = Modifier.fillMaxWidth().testTag("location_input")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 Auto-fetched current location: '${environment.locationLabel}'",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TriggerType.SCHEDULE -> {
                                    Text("⏰ Time Schedule & Routine", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = timeStart,
                                            onValueChange = { timeStart = it },
                                            label = { Text("Start Time") },
                                            modifier = Modifier.weight(1f).testTag("time_start_input")
                                        )
                                        OutlinedTextField(
                                            value = timeEnd,
                                            onValueChange = { timeEnd = it },
                                            label = { Text("End Time") },
                                            modifier = Modifier.weight(1f).testTag("time_end_input")
                                        )
                                    }
                                }
                                TriggerType.CONNECTIVITY -> {
                                    Text("📶 Wi-Fi or Bluetooth Connection", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = wifiSsid,
                                        onValueChange = { wifiSsid = it },
                                        label = { Text("Wi-Fi SSID Name") },
                                        modifier = Modifier.fillMaxWidth().testTag("wifi_ssid_input")
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "💡 Auto-detected Wi-Fi: '${environment.wifiSsid}'",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = bluetoothDevice,
                                        onValueChange = { bluetoothDevice = it },
                                        label = { Text("Bluetooth Device Name (Car, Earbuds)") },
                                        modifier = Modifier.fillMaxWidth().testTag("bt_device_input")
                                    )
                                }
                                TriggerType.BATTERY -> {
                                    Text("🔋 Battery Threshold & Power", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Threshold: Below $batteryThreshold%")
                                    Slider(
                                        value = batteryThreshold.toFloat(),
                                        onValueChange = { batteryThreshold = it.toInt() },
                                        valueRange = 5f..50f
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Also trigger when charger is plugged in")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = batteryIsCharging,
                                            onCheckedChange = { batteryIsCharging = it }
                                        )
                                    }
                                }
                                TriggerType.APP_OPEN -> {
                                    Text("📲 App Open Context", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = appName,
                                        onValueChange = { appName = it },
                                        label = { Text("Target App Name (e.g., YouTube, Spotify, Maps)") },
                                        modifier = Modifier.fillMaxWidth().testTag("app_name_input")
                                    )
                                }
                                TriggerType.ACTIVITY -> {
                                    Text("🚶 Activity Recognition", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Walking", "Running", "Driving", "Stationary").forEach { act ->
                                            FilterChip(
                                                selected = activityType == act,
                                                onClick = { activityType = act },
                                                label = { Text(act) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { currentStep = 1 },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                        ) {
                            Text("← Back")
                        }
                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    title = when (triggerType) {
                                        TriggerType.LOCATION -> "Arriving at $locationLabel"
                                        TriggerType.SCHEDULE -> "Daily Routine ($timeStart)"
                                        TriggerType.CONNECTIVITY -> "Connected to $wifiSsid"
                                        TriggerType.BATTERY -> "Battery Low ($batteryThreshold%)"
                                        TriggerType.APP_OPEN -> "Opened $appName"
                                        TriggerType.ACTIVITY -> "Activity Mode ($activityType)"
                                    }
                                }
                                if (description.isBlank()) {
                                    description = "Automated flow triggered by ${triggerType.displayName}"
                                }
                                currentStep = 3
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("step2_next_btn")
                        ) {
                            Text("Next: Save Flow →")
                        }
                    }
                }

                // STEP 3: NAME, ICON & SAVE
                3 -> {
                    Text(
                        text = "Step 3: Name & Save Your Flow",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Customize the visual appearance for your flow card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Flow Name") },
                        modifier = Modifier.fillMaxWidth().testTag("flow_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description") },
                        modifier = Modifier.fillMaxWidth().testTag("flow_desc_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🏷️ Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Lifestyle", "Work", "Battery", "Media", "Travel").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("🎨 Choose Icon & Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Bedtime", "Work", "FitnessCenter", "DirectionsCar", "BatterySaver", "PlayCircle", "Home", "Wifi").forEach { iconKey ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (iconName == iconKey) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(if (iconName == iconKey) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable { iconName = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFlowIconVector(iconKey),
                                    contentDescription = iconKey,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("⏱️ Delayed Start", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Trigger actions after a delay once condition is met.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Instant", 5 to "5s", 15 to "15s", 30 to "30s", 60 to "1m", 300 to "5m").forEach { (sec, label) ->
                            FilterChip(
                                selected = delayedSeconds == sec,
                                onClick = { delayedSeconds = sec },
                                label = { Text(label) },
                                modifier = Modifier.testTag("delay_${sec}s")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val triggerCfg = TriggerConfig(
                                type = triggerType,
                                locationLabel = locationLabel,
                                timeStart = timeStart,
                                timeEnd = timeEnd,
                                wifiSsid = wifiSsid,
                                bluetoothDeviceName = bluetoothDevice,
                                batteryThreshold = batteryThreshold,
                                batteryIsCharging = batteryIsCharging,
                                appName = appName,
                                activityType = activityType
                            )
                            val actionCfg = ActionConfig(
                                ringtoneMode = ringtoneMode,
                                setMediaVolume = setMediaVolume,
                                mediaVolumePercent = mediaVolumePercent,
                                enableDnd = enableDnd,
                                setBrightness = setBrightness,
                                brightnessPercent = brightnessPercent,
                                enableDarkMode = enableDarkMode,
                                toggleWifi = toggleWifi,
                                wifiState = wifiState,
                                toggleBluetooth = toggleBluetooth,
                                bluetoothState = bluetoothState,
                                enableAutoRotate = enableAutoRotate,
                                announceTts = announceTts,
                                ttsMessage = ttsMessage,
                                delayedSeconds = delayedSeconds
                            )
                            onSaveFlow(
                                editingFlow?.id,
                                title,
                                description,
                                category,
                                iconName,
                                colorHex,
                                triggerType,
                                triggerCfg,
                                actionCfg,
                                timeStart
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("save_flow_submit_btn")
                    ) {
                        Text("Save & Activate Flow", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
