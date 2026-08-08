package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ActionConfig
import com.example.data.TriggerConfig
import com.example.data.TriggerType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateRoutineDialog(
    onDismiss: () -> Unit,
    onSaveRoutine: (
        title: String,
        description: String,
        category: String,
        iconName: String,
        colorHex: String,
        triggerType: TriggerType,
        scheduledTime: String,
        triggerConfig: TriggerConfig,
        actionConfig: ActionConfig,
        tags: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(1) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lifestyle") }
    var iconName by remember { mutableStateOf("Schedule") }
    var colorHex by remember { mutableStateOf("#818CF8") }
    var tagsInput by remember { mutableStateOf("General") }
    
    var triggerType by remember { mutableStateOf(TriggerType.SCHEDULE) }
    var scheduledTime by remember { mutableStateOf("08:00") }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Trigger details
    var locationLabel by remember { mutableStateOf("Home") }
    var wifiSsid by remember { mutableStateOf("Home-WiFi") }
    var bluetoothDevice by remember { mutableStateOf("Car-Kit") }
    var batteryThreshold by remember { mutableIntStateOf(20) }
    var appName by remember { mutableStateOf("YouTube") }
    var activityType by remember { mutableStateOf("Walking") }
    var useAlarmManager by remember { mutableStateOf(false) }

    // Action details
    var ringtoneMode by remember { mutableStateOf("SILENT") }
    var setMediaVolume by remember { mutableStateOf(true) }
    var mediaVolumePercent by remember { mutableIntStateOf(50) }
    var enableDnd by remember { mutableStateOf(true) }
    var setBrightness by remember { mutableStateOf(false) }
    var brightnessPercent by remember { mutableIntStateOf(50) }
    var announceTts by remember { mutableStateOf(false) }
    var ttsMessage by remember { mutableStateOf("Routine executed") }

    val iconList = listOf(
        "Schedule", "Alarm", "Bedtime", "Work", "FitnessCenter",
        "DirectionsCar", "BatterySaver", "PlayCircle", "Home",
        "Wifi", "Bluetooth", "LocationOn", "VolumeUp", "Brightness6", "NotificationsActive"
    )

    // Helper map of category defaults
    val categoryDefaults = mapOf(
        "Home" to Pair("Home", "#10B981"),
        "Work" to Pair("Work", "#38BDF8"),
        "Health" to Pair("FitnessCenter", "#EC4899"),
        "Lifestyle" to Pair("Bedtime", "#818CF8"),
        "Battery" to Pair("BatterySaver", "#F87171"),
        "Media" to Pair("PlayCircle", "#F59E0B"),
        "Travel" to Pair("DirectionsCar", "#10B981")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .testTag("create_routine_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Wizard Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zero-Logic Setup Wizard",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Simple Q&A to build your custom automation",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_create_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Indicator
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = currentStep / 4f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (currentStep) {
                            1 -> "Step 1 of 4: Routine Identity & Style"
                            2 -> "Step 2 of 4: Set the Condition (When)"
                            3 -> "Step 3 of 4: Set the Actions (What)"
                            else -> "Step 4 of 4: Review & Finalize"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Wizard Steps Content
                when (currentStep) {
                    1 -> {
                        // STEP 1: IDENTITY & STYLE
                        Text(
                            text = "First, let's classify and name your routine:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Selection Grid
                        Text(
                            text = "📂 Select Category System",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Home", "Work", "Health", "Lifestyle", "Battery", "Media", "Travel").forEach { cat ->
                                val isSelected = category == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        category = cat
                                        // Auto-apply defaults to simplify things for zero-logic
                                        categoryDefaults[cat]?.let { (defaultIcon, defaultColor) ->
                                            iconName = defaultIcon
                                            colorHex = defaultColor
                                        }
                                        if (tagsInput.isBlank() || tagsInput == "General") {
                                            tagsInput = cat
                                        }
                                    },
                                    label = { Text(cat) },
                                    modifier = Modifier.testTag("wizard_category_chip_$cat")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Routine Name input
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("What is this routine called? *") },
                            placeholder = { Text("e.g. Arrive at Office, Low Battery Shield") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_routine_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description input
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("What does it do? (Optional)") },
                            placeholder = { Text("e.g. Mute phone and dim brightness") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_routine_desc_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tags input
                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            label = { Text("Tags (comma separated)") },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dialog_routine_tags_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Icon Selection
                        Text(
                            text = "🎨 Custom Representative Icon",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            iconList.forEach { key ->
                                val isSelected = iconName == key
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                        .border(
                                            if (isSelected) 2.dp else 0.dp,
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                        .clickable { iconName = key }
                                        .testTag("dialog_icon_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getFlowIconVector(key),
                                        contentDescription = key,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Custom Color Accent Selection
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🎨 Custom Accent Color",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val colorOptions = listOf(
                            "#818CF8", // Indigo
                            "#10B981", // Emerald
                            "#38BDF8", // Sky Blue
                            "#EC4899", // Pink
                            "#F87171", // Light Red
                            "#F59E0B", // Amber
                            "#A78BFA", // Purple
                            "#14B8A6", // Teal
                            "#F43F5E"  // Rose
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            colorOptions.forEach { hex ->
                                val color = parseColorHex(hex)
                                val isSelected = colorHex.uppercase(Locale.getDefault()) == hex.uppercase(Locale.getDefault())
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { colorHex = hex }
                                        .testTag("color_picker_$hex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // STEP 2: TRIGGER CONDITION
                        Text(
                            text = "When should this routine trigger automatically?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Trigger Type selector chips
                        Text(
                            text = "⚡ Choose Trigger Event",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TriggerType.entries.forEach { type ->
                                val isSelected = triggerType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { triggerType = type },
                                    label = { Text(type.displayName) },
                                    modifier = Modifier.testTag("wizard_trigger_chip_${type.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic trigger custom configuration cards
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                when (triggerType) {
                                    TriggerType.SCHEDULE -> {
                                        Text("⏰ Time Schedule Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("At what time should this run?", style = MaterialTheme.typography.bodyMedium)
                                                Text("Selected: $scheduledTime", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            OutlinedButton(
                                                onClick = { showTimePickerDialog = true },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.testTag("open_time_picker_btn")
                                            ) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Select Time")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Use Precise Alarm (AlarmManager)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("Triggers routine exactly at scheduled time even when phone is idle/sleeping.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(
                                                checked = useAlarmManager,
                                                onCheckedChange = { useAlarmManager = it },
                                                modifier = Modifier.testTag("dialog_alarm_manager_switch")
                                            )
                                        }
                                    }
                                    TriggerType.LOCATION -> {
                                        Text("📍 Location Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("When arriving or leaving what location?", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = locationLabel,
                                            onValueChange = { locationLabel = it },
                                            placeholder = { Text("e.g. Home, Office, Gym") },
                                            modifier = Modifier.fillMaxWidth().testTag("dialog_location_input")
                                        )
                                    }
                                    TriggerType.CONNECTIVITY -> {
                                        Text("📡 Connectivity Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("When connected to which Wi-Fi SSID?", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = wifiSsid,
                                            onValueChange = { wifiSsid = it },
                                            placeholder = { Text("e.g. Office-Wifi") },
                                            modifier = Modifier.fillMaxWidth().testTag("dialog_wifi_input")
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("When connected to which Bluetooth Device?", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = bluetoothDevice,
                                            onValueChange = { bluetoothDevice = it },
                                            placeholder = { Text("e.g. Car-Bluetooth") },
                                            modifier = Modifier.fillMaxWidth().testTag("dialog_bluetooth_input")
                                        )
                                    }
                                    TriggerType.BATTERY -> {
                                        Text("🔋 Battery Power Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("When battery levels drop below what percentage?", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$batteryThreshold%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Slider(
                                                value = batteryThreshold.toFloat(),
                                                onValueChange = { batteryThreshold = it.toInt() },
                                                valueRange = 5f..95f,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    TriggerType.APP_OPEN -> {
                                        Text("📱 App Launcher Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("When opening what app?", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = appName,
                                            onValueChange = { appName = it },
                                            placeholder = { Text("e.g. YouTube, Maps, Netflix") },
                                            modifier = Modifier.fillMaxWidth().testTag("dialog_app_input")
                                        )
                                    }
                                    TriggerType.ACTIVITY -> {
                                        Text("🏃 Physical Activity Q&A", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("When the device detects you are:", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            listOf("Walking", "Driving", "Still").forEach { act ->
                                                val actSelected = activityType.equals(act, ignoreCase = true)
                                                FilterChip(
                                                    selected = actSelected,
                                                    onClick = { activityType = act },
                                                    label = { Text(act) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // STEP 3: ACTIONS TO PERFORM
                        Text(
                            text = "What actions should SwAIft take for you?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sound Profile Selection
                        Text(
                            text = "🔊 Sound Profile Toggle",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("NORMAL", "VIBRATE", "SILENT").forEach { mode ->
                                val isSelected = ringtoneMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { ringtoneMode = mode },
                                    label = { Text(mode) },
                                    modifier = Modifier.testTag("wizard_sound_chip_$mode")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // DND Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Do Not Disturb (DND)", fontWeight = FontWeight.Bold)
                                Text("Mute all incoming notification noises", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = enableDnd, onCheckedChange = { enableDnd = it })
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Media Volume Switch & Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Set Media Volume", fontWeight = FontWeight.Bold)
                                Text("Adjust volume specifically to: $mediaVolumePercent%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = setMediaVolume, onCheckedChange = { setMediaVolume = it })
                        }
                        if (setMediaVolume) {
                            Slider(
                                value = mediaVolumePercent.toFloat(),
                                onValueChange = { mediaVolumePercent = it.toInt() },
                                valueRange = 0f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Brightness Switch & Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Set Screen Brightness", fontWeight = FontWeight.Bold)
                                Text("Adjust display brightness specifically to: $brightnessPercent%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = setBrightness, onCheckedChange = { setBrightness = it })
                        }
                        if (setBrightness) {
                            Slider(
                                value = brightnessPercent.toFloat(),
                                onValueChange = { brightnessPercent = it.toInt() },
                                valueRange = 10f..100f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Text To Speech Announcement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Voice Announcement (TTS)", fontWeight = FontWeight.Bold)
                                Text("Speak a descriptive custom audio message", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = announceTts, onCheckedChange = { announceTts = it })
                        }
                        if (announceTts) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = ttsMessage,
                                onValueChange = { ttsMessage = it },
                                label = { Text("Announcement Speech Text") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    else -> {
                        // STEP 4: REVIEW SUMMARY
                        Text(
                            text = "Review your Zero-Logic Automation:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (title.isBlank()) "New Automated Routine" else title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (description.isNotBlank()) {
                                    Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "📂 Category: $category • 🏷️ Tags: $tagsInput",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Trigger Condition text
                                Text("WHEN:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = when (triggerType) {
                                        TriggerType.SCHEDULE -> "🕒 Scheduled daily at $scheduledTime"
                                        TriggerType.LOCATION -> "📍 Arriving or leaving around '$locationLabel'"
                                        TriggerType.CONNECTIVITY -> "📡 Connected to Wi-Fi SSID '$wifiSsid' or Bluetooth Device '$bluetoothDevice'"
                                        TriggerType.BATTERY -> "🔋 Battery levels drop below $batteryThreshold%"
                                        TriggerType.APP_OPEN -> "📱 Opening the '$appName' application"
                                        TriggerType.ACTIVITY -> "🏃 Physical activity detected as '$activityType'"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                // Action execution text
                                Text("THEN SWAIFT WILL AUTOMATICALLY:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                Column(
                                    modifier = Modifier.padding(top = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("• Set Sound Profile to $ringtoneMode", style = MaterialTheme.typography.bodyMedium)
                                    if (enableDnd) {
                                        Text("• Enable Do Not Disturb (DND)", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (setMediaVolume) {
                                        Text("• Change Media Volume to $mediaVolumePercent%", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (setBrightness) {
                                        Text("• Set Screen Brightness to $brightnessPercent%", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (announceTts) {
                                        Text("• Speak Announcement: \"$ttsMessage\"", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Wizard Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button / Cancel button
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dialog_cancel_btn")
                        ) {
                            Text("Cancel")
                        }
                    }

                    // Next / Create Routine button
                    if (currentStep < 4) {
                        Button(
                            onClick = {
                                if (currentStep == 1 && title.isBlank()) {
                                    title = "Automation Routine"
                                }
                                currentStep++
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("wizard_next_btn")
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                val finalTitle = if (title.isBlank()) "New Automation Routine" else title
                                val finalDesc = if (description.isBlank()) "Automated flow triggered by ${triggerType.displayName}" else description
                                val triggerCfg = TriggerConfig(
                                    type = triggerType,
                                    locationLabel = locationLabel,
                                    timeStart = scheduledTime,
                                    timeEnd = scheduledTime,
                                    wifiSsid = wifiSsid,
                                    bluetoothDeviceName = bluetoothDevice,
                                    batteryThreshold = batteryThreshold,
                                    appName = appName,
                                    activityType = activityType,
                                    useAlarmManager = useAlarmManager
                                )
                                val actionCfg = ActionConfig(
                                    ringtoneMode = ringtoneMode,
                                    setMediaVolume = setMediaVolume,
                                    mediaVolumePercent = mediaVolumePercent,
                                    enableDnd = enableDnd,
                                    setBrightness = setBrightness,
                                    brightnessPercent = brightnessPercent,
                                    announceTts = announceTts,
                                    ttsMessage = ttsMessage
                                )
                                onSaveRoutine(
                                    finalTitle,
                                    finalDesc,
                                    category,
                                    iconName,
                                    colorHex,
                                    triggerType,
                                    scheduledTime,
                                    triggerCfg,
                                    actionCfg,
                                    tagsInput.ifBlank { "General" }
                                )
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("dialog_save_routine_btn")
                        ) {
                            Text("Create Routine", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        val initialHours = scheduledTime.split(":").getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinutes = scheduledTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHours,
            initialMinute = initialMinutes,
            is24Hour = true
        )

        Dialog(onDismissRequest = { showTimePickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Schedule Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePickerDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val formattedHour = String.format(Locale.getDefault(), "%02d", timePickerState.hour)
                                val formattedMin = String.format(Locale.getDefault(), "%02d", timePickerState.minute)
                                scheduledTime = "$formattedHour:$formattedMin"
                                showTimePickerDialog = false
                            },
                            modifier = Modifier.testTag("confirm_time_picker_btn")
                        ) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}
