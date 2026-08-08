package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryLogEntity
import com.example.ui.components.getFlowIconVector
import com.example.ui.components.parseColorHex
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    logs: List<HistoryLogEntity>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var exportFormat by remember { mutableStateOf("CSV") }
    var selectedDateFilter by remember { mutableStateOf("All") }

    val now = System.currentTimeMillis()
    val filteredLogs = remember(logs, selectedDateFilter) {
        when (selectedDateFilter) {
            "Today" -> {
                val startOfDay = now - (now % 86_400_000L)
                logs.filter { it.timestampMillis >= startOfDay }
            }
            "Last 7 Days" -> {
                val cutoff = now - (7L * 86_400_000L)
                logs.filter { it.timestampMillis >= cutoff }
            }
            "Last 30 Days" -> {
                val cutoff = now - (30L * 86_400_000L)
                logs.filter { it.timestampMillis >= cutoff }
            }
            else -> logs
        }
    }

    fun exportHistoryLogs() {
        val content = if (exportFormat == "CSV") {
            val csvHeader = "ID,Timestamp,Status,RoutineTitle,TriggerReason,ActionsExecuted\n"
            val csvBody = logs.joinToString("\n") { log ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestampMillis))
                "\"${log.id}\",\"$dateStr\",\"${log.status}\",\"${log.flowTitle.replace("\"", "\"\"")}\",\"${log.triggerReason.replace("\"", "\"\"")}\",\"${log.actionsExecuted.replace("\"", "\"\"")}\""
            }
            csvHeader + csvBody
        } else {
            logs.joinToString(prefix = "[\n", postfix = "\n]", separator = ",\n") { log ->
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestampMillis))
                "  {\"id\": \"${log.id}\", \"timestamp\": \"$dateStr\", \"status\": \"${log.status}\", \"title\": \"${log.flowTitle}\", \"trigger\": \"${log.triggerReason}\", \"actions\": \"${log.actionsExecuted}\"}"
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, if (exportFormat == "CSV") "SwAIft_Activity_Log.csv" else "SwAIft_Activity_Log.json")
            type = if (exportFormat == "CSV") "text/csv" else "application/json"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Activity Log ($exportFormat)")
        context.startActivity(shareIntent)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp),
            contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
        ) {
            // Transparency Banner Header
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("history_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Transparency Mode Diary",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Complete record showing exactly what SwAIft executed and why.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Format:", style = MaterialTheme.typography.labelSmall)
                                listOf("CSV", "JSON").forEach { fmt ->
                                    FilterChip(
                                        selected = exportFormat == fmt,
                                        onClick = { exportFormat = fmt },
                                        label = { Text(fmt, fontSize = 12.sp) },
                                        modifier = Modifier.height(32.dp).testTag("export_format_$fmt")
                                    )
                                }
                            }

                            if (logs.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { exportHistoryLogs() },
                                        modifier = Modifier.testTag("export_history_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Export History Log",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = onClearLogs,
                                        modifier = Modifier.testTag("clear_history_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteSweep,
                                            contentDescription = "Clear Log",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Date Filter Row
            if (logs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        listOf("All", "Today", "Last 7 Days", "Last 30 Days").forEach { filter ->
                            FilterChip(
                                selected = selectedDateFilter == filter,
                                onClick = { selectedDateFilter = filter },
                                label = { Text(filter, fontSize = 11.sp) },
                                modifier = Modifier.height(30.dp).testTag("date_filter_$filter")
                            )
                        }
                    }
                }
            }

            if (filteredLogs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyHistoryIllustration()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (logs.isEmpty()) "History Log is Clear" else "No Logs Match Filter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (logs.isEmpty()) "When automated routines run in response to time, location, or battery triggers, exact records will appear here." else "Try selecting a different date range filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    HistoryItemCard(
                        log = log,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (logs.isNotEmpty()) {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = {
                    exportFormat = "JSON"
                    exportHistoryLogs()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("export_json_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export JSON Logs"
                    )
                },
                text = {
                    Text("Export JSON", fontWeight = FontWeight.Bold)
                }
            )
        }
    }
}

@Composable
fun HistoryItemCard(
    log: HistoryLogEntity,
    modifier: Modifier = Modifier
) {
    val themeColor = parseColorHex(log.colorHex)
    val sdf = SimpleDateFormat("MMM d, yyyy • HH:mm:ss", Locale.getDefault())
    val dateStr = sdf.format(Date(log.timestampMillis))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("history_item_${log.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFlowIconVector(log.iconName),
                        contentDescription = log.flowTitle,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.flowTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (log.status) {
                                "SUCCESS" -> MaterialTheme.colorScheme.primaryContainer
                                "MANUAL" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.status,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (log.status) {
                            "SUCCESS" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "MANUAL" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trigger Reason
            Text(
                text = "WHY: ${log.triggerReason}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Actions executed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ACTIONS: ${log.actionsExecuted}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryIllustration() {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(100.dp)
    ) {
        val width = size.width
        drawCircle(
            color = surfaceVariant.copy(alpha = 0.6f),
            radius = width / 2f
        )
        drawCircle(
            color = secondaryColor.copy(alpha = 0.25f),
            radius = width / 3f
        )
        drawCircle(
            color = secondaryColor,
            radius = width / 6f
        )
    }
}
