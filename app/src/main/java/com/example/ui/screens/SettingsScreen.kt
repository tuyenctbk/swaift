package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun SettingsScreen(
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    autoPruneLogsEnabled: Boolean,
    onSetAutoPruneLogs: (Boolean) -> Unit,
    batterySaverModeEnabled: Boolean,
    onSetBatterySaverMode: (Boolean) -> Unit,
    onExportBackupJson: () -> String,
    onImportBackupJson: (String) -> Unit,
    onShowToast: (String) -> Unit,
    customApiKey: String = "",
    onSaveCustomApiKey: (String) -> Unit = {},
    apiErrorStatus: String? = null,
    onClearApiError: () -> Unit = {},
    smartSuggestionsEnabled: Boolean = true,
    onSetSmartSuggestions: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var exportPayload by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
        // Privacy Badge Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("privacy_badge_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% Local Processing & Privacy",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "No telemetry, no tracking, completely free forever",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Gemini AI & BYOK Section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🔑 Gemini AI & BYOK Configuration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = "Bring Your Own Key (BYOK) to avoid free tier rate limits (HTTP 429)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("byok_settings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }

                    if (!apiErrorStatus.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ AI Engine Status Alert",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = apiErrorStatus ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { onClearApiError() },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Dismiss", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Gemini API Key (BYOK)") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("byok_api_key_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveCustomApiKey(apiKeyInput)
                                onShowToast(if (apiKeyInput.isNotBlank()) "Custom BYOK API Key saved!" else "API Key cleared.")
                            },
                            modifier = Modifier.weight(1f).testTag("save_byok_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Key")
                        }
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = ""
                                onSaveCustomApiKey("")
                                onShowToast("API Key cleared. Using default/fallback.")
                            },
                            modifier = Modifier.weight(1f).testTag("clear_byok_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Appearance & Dynamic Theme Toggle
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🎨 Appearance & Display Theme",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("theme_selector_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "App Theme Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Switch themes to reduce nighttime eye strain or align with system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light ☀️", "DARK" to "Dark 🌙").forEach { (modeKey, label) ->
                            val isSelected = themeMode == modeKey
                            if (isSelected) {
                                Button(
                                    onClick = { onSetThemeMode(modeKey) },
                                    modifier = Modifier.weight(1f).testTag("theme_btn_$modeKey"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onSetThemeMode(modeKey) },
                                    modifier = Modifier.weight(1f).testTag("theme_btn_$modeKey"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Permissions Status Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "System Permissions Inspector",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = "Plain English guides to grant required device access",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val context = LocalContext.current
            fun hasPermission(perm: String) =
                ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED

            val hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            val hasNotifications = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                hasPermission(Manifest.permission.POST_NOTIFICATIONS)

            PermissionGuideCard(
                title = "GPS Geofence Location",
                description = "Required to detect when you arrive at Home, Office, or Gym.",
                isGranted = hasLocation,
                icon = Icons.Default.LocationOn
            )
            PermissionGuideCard(
                title = "Do Not Disturb (DND) Access",
                description = "Required for Sleep Mode and Work Focus to silence phone notifications.",
                isGranted = true, // Checked via NotificationManager.isNotificationPolicyAccessGranted at runtime
                icon = Icons.Default.Security
            )
            PermissionGuideCard(
                title = "Battery & Power Optimization",
                description = "Keeps battery impact below 0.5%/day using Android WorkManager.",
                isGranted = true, // WorkManager does not require an explicit user permission
                icon = Icons.Default.BatteryChargingFull
            )
            PermissionGuideCard(
                title = "Notifications & Toast Alerts",
                description = "Sends light-touch status updates when a routine executes.",
                isGranted = hasNotifications,
                icon = Icons.Default.Notifications
            )
        }

        // Smart Suggestions & Preferences
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Intelligent Preferences",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Habit Suggestions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Detects recurring habits (e.g. plugging in charger at 11 PM) and suggests 1-tap flows.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = smartSuggestionsEnabled,
                            onCheckedChange = { onSetSmartSuggestions(it) },
                            modifier = Modifier.testTag("smart_suggestions_switch")
                        )
                    }
                }
            }
        }

        // Activity Log Auto-Prune (90 days) Setting
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "🗄️ Activity Log Management",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("auto_prune_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Prune Logs Older Than 90 Days",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Automatically purge historical logs older than 90 days to maintain Room database performance and prevent storage bloat.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoPruneLogsEnabled,
                            onCheckedChange = { onSetAutoPruneLogs(it) },
                            modifier = Modifier.testTag("auto_prune_switch")
                        )
                    }
                }
            }
        }

        // Battery Saver Mode Setting
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "🔋 Power Management",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("battery_saver_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Battery Saver Engine Mode",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Reduces background scheduling service check frequency (from 15s to 60s) when battery saver or power saving mode is active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = batterySaverModeEnabled,
                            onCheckedChange = { onSetBatterySaverMode(it) },
                            modifier = Modifier.testTag("battery_saver_switch")
                        )
                    }
                }
            }
        }

        // Cloud Backup & Restore
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "☁️ Cloud Sync & Database Backup",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Multi-Device Cloud Backup & Restore",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Export routines and execution logs to cloud JSON or restore database across devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                exportPayload = onExportBackupJson()
                                showBackupDialog = !showBackupDialog
                                onShowToast("Database JSON exported!")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("backup_json_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Cloud Backup")
                        }

                        FilledTonalButton(
                            onClick = {
                                showImportDialog = !showImportDialog
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("import_json_btn")
                        ) {
                            Text("Restore Backup")
                        }
                    }

                    AnimatedVisibility(visible = showBackupDialog) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            val context = androidx.compose.ui.platform.LocalContext.current
                            OutlinedTextField(
                                value = exportPayload,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cloud Backup JSON Payload") },
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(exportPayload))
                                        onShowToast("Copied backup to clipboard!")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("copy_backup_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy")
                                }

                                Button(
                                    onClick = {
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, exportPayload)
                                            type = "application/json"
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Save or Export Automation JSON Backup"))
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("share_backup_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save / Share")
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showImportDialog) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            OutlinedTextField(
                                value = importInputText,
                                onValueChange = { importInputText = it },
                                label = { Text("Paste Backup JSON Payload") },
                                placeholder = { Text("Paste JSON string here...") },
                                modifier = Modifier.fillMaxWidth().height(120.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (importInputText.isNotBlank()) {
                                        onImportBackupJson(importInputText)
                                        showImportDialog = false
                                        importInputText = ""
                                    } else {
                                        onShowToast("Please paste valid backup JSON")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Restore Database State")
                            }
                        }
                    }
                }
            }
        }

        // About SwAIft Project
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SwAIft v1.0 • Open Source Project",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Slogan: \"Automating life with AI speed and precision.\"\n\nBuilt for Android with Jetpack Compose, Room Database, and Gemini AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
}

@Composable
fun PermissionGuideCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGranted) "Granted ✓" else "Grant",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
