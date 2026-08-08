package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.engine.SimulatedEnvironment

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentDrawer(
    environment: SimulatedEnvironment,
    onDismiss: () -> Unit,
    onUpdateLocation: (String) -> Unit,
    onUpdateWifi: (String) -> Unit,
    onUpdateBluetooth: (String) -> Unit,
    onUpdateBattery: (Int, Boolean) -> Unit,
    onUpdateApp: (String) -> Unit,
    onUpdateActivity: (String) -> Unit,
    onUpdateTime: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📱 ${stringResource(R.string.env_conditions_title)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Simulate triggers to test flows live",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.done), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            Text("📍 ${stringResource(R.string.env_gps_location)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home", "Office", "Fitness Center", "Downtown").forEach { loc ->
                    val isSelected = environment.locationLabel.equals(loc, ignoreCase = true)
                    AssistChip(
                        onClick = { onUpdateLocation(loc) },
                        label = { Text(loc, maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("sim_loc_$loc")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wi-Fi Connection
            Text("📶 ${stringResource(R.string.env_wifi_ssid)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home-WiFi-5G", "Office-Corporate-WiFi", "Disconnected").forEach { wifi ->
                    AssistChip(
                        onClick = { onUpdateWifi(wifi) },
                        label = { Text(if (wifi == "Disconnected") stringResource(R.string.status_none) else wifi, maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("sim_wifi_$wifi")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Battery Level & Charging
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔋 Battery Level (${environment.batteryPercent}%)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.env_charger_plugged), style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = environment.isCharging,
                        onCheckedChange = { isCharging -> onUpdateBattery(environment.batteryPercent, isCharging) },
                        modifier = Modifier.testTag("sim_charging_switch")
                    )
                }
            }
            Slider(
                value = environment.batteryPercent.toFloat(),
                onValueChange = { valInt -> onUpdateBattery(valInt.toInt(), environment.isCharging) },
                valueRange = 5f..100f,
                steps = 19,
                modifier = Modifier.testTag("sim_battery_slider")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // App Open Context
            Text("📲 ${stringResource(R.string.env_open_app_context)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home Screen", "YouTube", "Maps", "Spotify").forEach { appName ->
                    AssistChip(
                        onClick = { onUpdateApp(appName) },
                        label = { Text(appName, maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("sim_app_$appName")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity Recognition
            Text("🚶 ${stringResource(R.string.env_activity_motion)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Stationary", "Walking", "Driving").forEach { activity ->
                    AssistChip(
                        onClick = { onUpdateActivity(activity) },
                        label = { Text(activity, maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("sim_act_$activity")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time of day
            Text("⏰ ${stringResource(R.string.env_time_schedule)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("08:00", "14:30", "20:00", "23:15").forEach { timeStr ->
                    AssistChip(
                        onClick = { onUpdateTime(timeStr) },
                        label = { Text(timeStr, maxLines = 1) },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("sim_time_$timeStr")
                    )
                }
            }
        }
    }
}
