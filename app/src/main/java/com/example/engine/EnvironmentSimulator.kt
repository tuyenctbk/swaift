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
    private val _environment = MutableStateFlow(SimulatedEnvironment())
    val environment: StateFlow<SimulatedEnvironment> = _environment.asStateFlow()

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
