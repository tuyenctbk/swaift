package com.example.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SimulatedEnvironment(
    val locationLabel: String = "Home",
    val wifiSsid: String = "Home-WiFi-5G",
    val bluetoothDevice: String = "Disconnected",
    val batteryPercent: Int = 75,
    val isCharging: Boolean = false,
    val currentApp: String = "Home Screen",
    val activityType: String = "Stationary",
    val timeOfDay: String = "14:30" // HH:mm format
)

object EnvironmentSimulator {
    private val _environment = MutableStateFlow(createInitialRealEnvironment())
    val environment: StateFlow<SimulatedEnvironment> = _environment.asStateFlow()

    private fun createInitialRealEnvironment(): SimulatedEnvironment {
        val nowStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        return SimulatedEnvironment(
            locationLabel = "Current Location",
            wifiSsid = "Active Wi-Fi",
            bluetoothDevice = "Disconnected",
            batteryPercent = 100,
            isCharging = false,
            currentApp = "SwAIft",
            activityType = "Stationary",
            timeOfDay = nowStr
        )
    }

    fun syncWithRealDevice(context: android.content.Context) {
        try {
            val nowStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            val batteryStatus: android.content.Intent? = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val status: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging: Boolean = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == android.os.BatteryManager.BATTERY_STATUS_FULL
            val level: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else _environment.value.batteryPercent

            _environment.value = _environment.value.copy(
                timeOfDay = nowStr,
                batteryPercent = batteryPct,
                isCharging = isCharging
            )
        } catch (e: Throwable) {
            // Fallback gracefully
        }
    }

    fun updateLocation(label: String) {
        _environment.value = _environment.value.copy(locationLabel = label)
    }

    fun updateWifi(ssid: String) {
        _environment.value = _environment.value.copy(wifiSsid = ssid)
    }

    fun updateBluetooth(device: String) {
        _environment.value = _environment.value.copy(bluetoothDevice = device)
    }

    fun updateBattery(percent: Int, isCharging: Boolean) {
        _environment.value = _environment.value.copy(batteryPercent = percent, isCharging = isCharging)
    }

    fun updateApp(appName: String) {
        _environment.value = _environment.value.copy(currentApp = appName)
    }

    fun updateActivity(activity: String) {
        _environment.value = _environment.value.copy(activityType = activity)
    }

    fun updateTime(time: String) {
        _environment.value = _environment.value.copy(timeOfDay = time)
    }
}
