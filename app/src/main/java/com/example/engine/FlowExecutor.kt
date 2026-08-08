package com.example.engine

import com.example.data.ActionConfig
import com.example.data.FlowEntity
import com.example.data.HistoryLogEntity
import com.example.data.JsonUtils
import com.example.data.TriggerConfig
import com.example.data.TriggerType

data class ExecutionResult(
    val flowTitle: String,
    val triggerReason: String,
    val actionsSummary: String,
    val isSuccess: Boolean = true
)

object FlowExecutor {

    fun executeFlow(
        flow: FlowEntity,
        reasonOverride: String? = null
    ): ExecutionResult {
        val trigger = JsonUtils.deserializeTrigger(flow.triggerConfigJson)
        val action = JsonUtils.deserializeAction(flow.actionConfigJson)

        val triggerReason = reasonOverride ?: generateTriggerReason(flow.triggerType, trigger)
        val actionsSummary = generateActionSummary(action)

        return ExecutionResult(
            flowTitle = flow.title,
            triggerReason = triggerReason,
            actionsSummary = actionsSummary,
            isSuccess = true
        )
    }

    fun isTriggerMatched(
        flow: FlowEntity,
        env: SimulatedEnvironment
    ): Boolean {
        if (!flow.isEnabled) return false
        val trigger = JsonUtils.deserializeTrigger(flow.triggerConfigJson)

        return when (flow.triggerType) {
            TriggerType.LOCATION -> {
                env.locationLabel.equals(trigger.locationLabel, ignoreCase = true)
            }
            TriggerType.SCHEDULE -> {
                // Match when current time falls within the scheduled window [timeStart, timeEnd]
                // Simple HH:mm lexicographic comparison works for same-day windows;
                // for overnight windows (e.g. 22:00–07:00) we handle wrap-around.
                val current = env.timeOfDay
                val start = trigger.timeStart
                val end = trigger.timeEnd
                if (start <= end) {
                    current >= start && current <= end
                } else {
                    // Overnight window wraps midnight (e.g. 22:00 → 07:00)
                    current >= start || current <= end
                }
            }
            TriggerType.CONNECTIVITY -> {
                val wifiMatch = trigger.wifiSsid.isNotBlank() && env.wifiSsid.contains(trigger.wifiSsid, ignoreCase = true)
                val btMatch = trigger.bluetoothDeviceName.isNotBlank() && env.bluetoothDevice.contains(trigger.bluetoothDeviceName, ignoreCase = true)
                wifiMatch || btMatch
            }
            TriggerType.BATTERY -> {
                if (trigger.batteryIsCharging) {
                    env.isCharging
                } else {
                    env.batteryPercent <= trigger.batteryThreshold
                }
            }
            TriggerType.APP_OPEN -> {
                env.currentApp.equals(trigger.appName, ignoreCase = true)
            }
            TriggerType.ACTIVITY -> {
                env.activityType.equals(trigger.activityType, ignoreCase = true)
            }
        }
    }

    private fun generateTriggerReason(type: TriggerType, trigger: TriggerConfig): String {
        return when (type) {
            TriggerType.LOCATION -> "Arrived at location '${trigger.locationLabel}'"
            TriggerType.SCHEDULE -> "Scheduled time reached (${trigger.timeStart})"
            TriggerType.CONNECTIVITY -> {
                if (trigger.wifiSsid.isNotBlank()) "Connected to Wi-Fi '${trigger.wifiSsid}'"
                else "Connected to Bluetooth '${trigger.bluetoothDeviceName}'"
            }
            TriggerType.BATTERY -> {
                if (trigger.batteryIsCharging) "Charger plugged in"
                else "Battery dropped below ${trigger.batteryThreshold}%"
            }
            TriggerType.APP_OPEN -> "Opened app '${trigger.appName}'"
            TriggerType.ACTIVITY -> "Activity detected: ${trigger.activityType}"
        }
    }

    fun generateActionSummary(action: ActionConfig): String {
        val actions = mutableListOf<String>()
        if (action.ringtoneMode != "NORMAL") {
            actions.add("Ringer set to ${action.ringtoneMode}")
        }
        if (action.enableDnd) {
            actions.add("Do Not Disturb ON")
        }
        if (action.setMediaVolume) {
            actions.add("Media Volume ${action.mediaVolumePercent}%")
        }
        if (action.setBrightness) {
            actions.add("Brightness ${action.brightnessPercent}%")
        }
        if (action.enableDarkMode) {
            actions.add("Dark Mode ON")
        }
        if (action.toggleWifi) {
            val state = if (action.wifiState) "ON" else "OFF"
            actions.add("Wi-Fi $state")
        }
        if (action.toggleBluetooth) {
            val state = if (action.bluetoothState) "ON" else "OFF"
            actions.add("Bluetooth $state")
        }
        if (action.enableAutoRotate) {
            actions.add("Auto-Rotate ON")
        }
        if (action.announceTts) {
            actions.add("TTS Alert: \"${action.ttsMessage}\"")
        }
        if (action.delayedSeconds > 0) {
            val delayText = if (action.delayedSeconds >= 60) "${action.delayedSeconds / 60}m delay" else "${action.delayedSeconds}s delay"
            actions.add(delayText)
        }
        return if (actions.isEmpty()) "Applied default system profile" else actions.joinToString(" • ")
    }
}
