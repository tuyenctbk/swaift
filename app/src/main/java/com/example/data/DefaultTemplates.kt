package com.example.data

object DefaultTemplates {
    fun getPrepopulatedFlows(): List<FlowEntity> {
        val sleepTrigger = TriggerConfig(
            type = TriggerType.SCHEDULE,
            timeStart = "23:00",
            timeEnd = "07:00",
            batteryIsCharging = true
        )
        val sleepAction = ActionConfig(
            ringtoneMode = "SILENT",
            enableDnd = true,
            setBrightness = true,
            brightnessPercent = 10,
            enableDarkMode = true
        )

        val workTrigger = TriggerConfig(
            type = TriggerType.LOCATION,
            locationLabel = "Office",
            radiusMeters = 250,
            wifiSsid = "Office-Corporate-WiFi"
        )
        val workAction = ActionConfig(
            ringtoneMode = "VIBRATE",
            setMediaVolume = true,
            mediaVolumePercent = 20,
            toggleBluetooth = true,
            bluetoothState = false
        )

        val gymTrigger = TriggerConfig(
            type = TriggerType.CONNECTIVITY,
            bluetoothDeviceName = "Sport-Earbuds-BT",
            locationLabel = "Fitness Center"
        )
        val gymAction = ActionConfig(
            setMediaVolume = true,
            mediaVolumePercent = 90,
            announceTts = true,
            ttsMessage = "Gym Mode Activated. Time to focus!"
        )

        val drivingTrigger = TriggerConfig(
            type = TriggerType.ACTIVITY,
            activityType = "Driving",
            bluetoothDeviceName = "Car-Audio-BT"
        )
        val drivingAction = ActionConfig(
            toggleBluetooth = true,
            bluetoothState = true,
            setMediaVolume = true,
            mediaVolumePercent = 85,
            announceTts = true,
            ttsMessage = "Driving Companion Active. Safe journey!"
        )

        val lowBatteryTrigger = TriggerConfig(
            type = TriggerType.BATTERY,
            batteryThreshold = 20,
            batteryIsCharging = false
        )
        val lowBatteryAction = ActionConfig(
            setBrightness = true,
            brightnessPercent = 15,
            enableDarkMode = true,
            toggleBluetooth = true,
            bluetoothState = false
        )

        val youtubeTrigger = TriggerConfig(
            type = TriggerType.APP_OPEN,
            appName = "YouTube"
        )
        val youtubeAction = ActionConfig(
            setMediaVolume = true,
            mediaVolumePercent = 80,
            enableAutoRotate = true,
            setBrightness = true,
            brightnessPercent = 90
        )

        val homeTrigger = TriggerConfig(
            type = TriggerType.CONNECTIVITY,
            wifiSsid = "Home-WiFi-5G",
            locationLabel = "Home"
        )
        val homeAction = ActionConfig(
            ringtoneMode = "NORMAL",
            setMediaVolume = true,
            mediaVolumePercent = 65,
            toggleWifi = true,
            wifiState = true
        )

        val focusTrigger = TriggerConfig(
            type = TriggerType.SCHEDULE,
            timeStart = "09:00",
            timeEnd = "12:00"
        )
        val focusAction = ActionConfig(
            ringtoneMode = "SILENT",
            enableDnd = true
        )

        return listOf(
            // Pre-activated User Flows (Active out of the box for immediate value)
            FlowEntity(
                id = "flow_sleep_mode",
                title = "Sleep Mode",
                description = "Silence ringer, enable DND, and dim screen from 11 PM to 7 AM when charging",
                category = "Lifestyle",
                iconName = "Bedtime",
                colorHex = "#818CF8",
                isEnabled = true,
                isTemplate = false,
                triggerType = TriggerType.SCHEDULE,
                triggerConfigJson = JsonUtils.serializeTrigger(sleepTrigger),
                actionConfigJson = JsonUtils.serializeAction(sleepAction)
            ),
            FlowEntity(
                id = "flow_work_focus",
                title = "Work Silence",
                description = "Set phone to vibrate and mute media upon arriving at the Office",
                category = "Work",
                iconName = "Work",
                colorHex = "#38BDF8",
                isEnabled = true,
                isTemplate = false,
                triggerType = TriggerType.LOCATION,
                triggerConfigJson = JsonUtils.serializeTrigger(workTrigger),
                actionConfigJson = JsonUtils.serializeAction(workAction)
            ),
            FlowEntity(
                id = "flow_low_battery",
                title = "Low Battery Shield",
                description = "Dim screen & enable dark mode automatically when battery drops below 20%",
                category = "Battery",
                iconName = "BatterySaver",
                colorHex = "#F87171",
                isEnabled = true,
                isTemplate = false,
                triggerType = TriggerType.BATTERY,
                triggerConfigJson = JsonUtils.serializeTrigger(lowBatteryTrigger),
                actionConfigJson = JsonUtils.serializeAction(lowBatteryAction)
            ),

            // Gallery Templates (Discover Tab)
            FlowEntity(
                id = "tpl_sleep",
                title = "Sleep Sanctuary",
                description = "Block all notifications, dim display to 10%, and enable dark mode overnight.",
                category = "Lifestyle",
                iconName = "Bedtime",
                colorHex = "#818CF8",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.SCHEDULE,
                triggerConfigJson = JsonUtils.serializeTrigger(sleepTrigger),
                actionConfigJson = JsonUtils.serializeAction(sleepAction),
                scheduledTime = "23:00",
                tags = "Home, Sleep, Quiet"
            ),
            FlowEntity(
                id = "tpl_work",
                title = "Office Professional",
                description = "Switch ringer to vibrate, connect to corporate Wi-Fi, and lower media volume.",
                category = "Work",
                iconName = "Work",
                colorHex = "#38BDF8",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.LOCATION,
                triggerConfigJson = JsonUtils.serializeTrigger(workTrigger),
                actionConfigJson = JsonUtils.serializeAction(workAction),
                scheduledTime = "09:00",
                tags = "Work, Office, Security"
            ),
            FlowEntity(
                id = "tpl_gym",
                title = "Gym Workout Boost",
                description = "Max out headphone media volume and speak a motivating start message when earbuds connect.",
                category = "Lifestyle",
                iconName = "FitnessCenter",
                colorHex = "#34D399",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.CONNECTIVITY,
                triggerConfigJson = JsonUtils.serializeTrigger(gymTrigger),
                actionConfigJson = JsonUtils.serializeAction(gymAction),
                scheduledTime = "07:30",
                tags = "Fitness, Health, Media"
            ),
            FlowEntity(
                id = "tpl_driving",
                title = "Safe Driver Companion",
                description = "Auto-connect car Bluetooth, set safe media volume, and read caller alerts aloud.",
                category = "Travel",
                iconName = "DirectionsCar",
                colorHex = "#FBBF24",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.ACTIVITY,
                triggerConfigJson = JsonUtils.serializeTrigger(drivingTrigger),
                actionConfigJson = JsonUtils.serializeAction(drivingAction),
                scheduledTime = "08:15",
                tags = "Travel, Vehicle, Security"
            ),
            FlowEntity(
                id = "tpl_low_battery",
                title = "Emergency Battery Saver",
                description = "Drop brightness to 15%, turn off Bluetooth & Wi-Fi, and switch to dark theme.",
                category = "Battery",
                iconName = "BatterySaver",
                colorHex = "#F87171",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.BATTERY,
                triggerConfigJson = JsonUtils.serializeTrigger(lowBatteryTrigger),
                actionConfigJson = JsonUtils.serializeAction(lowBatteryAction),
                scheduledTime = "20:00",
                tags = "Battery, Emergency, System"
            ),
            FlowEntity(
                id = "tpl_youtube",
                title = "YouTube Cinema Mode",
                description = "Max screen brightness, enable auto-rotation, and set volume to 80% when launching YouTube.",
                category = "Media",
                iconName = "PlayCircle",
                colorHex = "#C084FC",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.APP_OPEN,
                triggerConfigJson = JsonUtils.serializeTrigger(youtubeTrigger),
                actionConfigJson = JsonUtils.serializeAction(youtubeAction),
                scheduledTime = "19:00",
                tags = "Media, Entertainment"
            ),
            FlowEntity(
                id = "tpl_home",
                title = "Home Welcome Routine",
                description = "Restore full ringer volume and auto-connect to Home Wi-Fi upon arrival.",
                category = "Lifestyle",
                iconName = "Home",
                colorHex = "#38BDF8",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.CONNECTIVITY,
                triggerConfigJson = JsonUtils.serializeTrigger(homeTrigger),
                actionConfigJson = JsonUtils.serializeAction(homeAction),
                scheduledTime = "18:00",
                tags = "Home, Security, Comfort"
            ),
            FlowEntity(
                id = "tpl_focus",
                title = "Deep Focus Hours",
                description = "Enable Do Not Disturb during morning peak focus hours (9 AM - 12 PM).",
                category = "Work",
                iconName = "Schedule",
                colorHex = "#A855F7",
                isEnabled = false,
                isTemplate = true,
                triggerType = TriggerType.SCHEDULE,
                triggerConfigJson = JsonUtils.serializeTrigger(focusTrigger),
                actionConfigJson = JsonUtils.serializeAction(focusAction),
                scheduledTime = "09:00",
                tags = "Work, Focus, Quiet"
            )
        )
    }
}
