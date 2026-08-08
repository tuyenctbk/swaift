package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TriggerType(val displayName: String, val description: String) {
    LOCATION("Location", "Triggers when arriving or leaving a GPS location"),
    SCHEDULE("Time Schedule", "Triggers at fixed times or daily routines"),
    CONNECTIVITY("Wi-Fi & Bluetooth", "Triggers when connecting/disconnecting devices"),
    BATTERY("Battery & Power", "Triggers when battery reaches threshold or plugs in"),
    APP_OPEN("App Open", "Triggers when a specific app is launched"),
    ACTIVITY("Activity Recognition", "Triggers when walking, driving, or stationary")
}

data class TriggerConfig(
    val type: TriggerType = TriggerType.SCHEDULE,
    val locationLabel: String = "Home",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val radiusMeters: Int = 200,
    val timeStart: String = "23:00", // HH:mm
    val timeEnd: String = "07:00",
    val wifiSsid: String = "Home-WiFi",
    val bluetoothDeviceName: String = "Car-Kit",
    val batteryThreshold: Int = 20,
    val batteryIsCharging: Boolean = false,
    val appName: String = "YouTube",
    val activityType: String = "Driving", // Walking, Running, Driving, Stationary
    val useAlarmManager: Boolean = false
)

data class ActionConfig(
    val muteRingtone: Boolean = false,
    val ringtoneMode: String = "SILENT", // NORMAL, VIBRATE, SILENT
    val setMediaVolume: Boolean = false,
    val mediaVolumePercent: Int = 50,
    val enableDnd: Boolean = false,
    val setBrightness: Boolean = false,
    val brightnessPercent: Int = 50,
    val enableDarkMode: Boolean = false,
    val toggleWifi: Boolean = false,
    val wifiState: Boolean = true,
    val toggleBluetooth: Boolean = false,
    val bluetoothState: Boolean = true,
    val enableAutoRotate: Boolean = false,
    val announceTts: Boolean = false,
    val ttsMessage: String = "Flow executed successfully",
    val delayedSeconds: Int = 0
)

@Entity(tableName = "flows")
data class FlowEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val category: String = "Lifestyle", // Lifestyle, Work, Battery, Media, Travel, Custom
    val iconName: String = "Bedtime",
    val colorHex: String = "#818CF8",
    val isEnabled: Boolean = true,
    val isTemplate: Boolean = false,
    val lastRunTimeMillis: Long? = null,
    val runCount: Int = 0,
    val triggerType: TriggerType = TriggerType.SCHEDULE,
    val triggerConfigJson: String = "",
    val actionConfigJson: String = "",
    val scheduledTime: String = "08:00",
    val tags: String = "General"
)
