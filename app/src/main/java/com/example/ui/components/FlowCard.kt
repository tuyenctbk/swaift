package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowEntity
import com.example.data.JsonUtils
import com.example.engine.FlowExecutor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.Surface

fun getFlowIconVector(iconName: String): ImageVector {
    return when (iconName) {
        "Bedtime" -> Icons.Default.Bedtime
        "Work" -> Icons.Default.Work
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "BatterySaver" -> Icons.Default.BatterySaver
        "PlayCircle" -> Icons.Default.PlayCircle
        "Home" -> Icons.Default.Home
        "Wifi" -> Icons.Default.Wifi
        "Bluetooth" -> Icons.Default.Bluetooth
        "LocationOn" -> Icons.Default.LocationOn
        "Schedule" -> Icons.Default.Schedule
        "Alarm" -> Icons.Default.Alarm
        "VolumeUp" -> Icons.AutoMirrored.Filled.VolumeUp
        "Brightness6" -> Icons.Default.Brightness6
        "NotificationsActive" -> Icons.Default.NotificationsActive
        else -> Icons.Default.Schedule
    }
}

fun parseColorHex(hex: String, fallback: Color = Color(0xFF818CF8)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowCard(
    flow: FlowEntity,
    onToggle: () -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    isTemplate: Boolean = false,
    onActivateTemplate: (() -> Unit)? = null,
    lastStatus: String? = null,
    isExecuting: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val themeColor = parseColorHex(flow.colorHex)
    val scale by animateFloatAsState(targetValue = if (flow.isEnabled) 1.0f else 0.98f, label = "cardScale")
    val haptic = LocalHapticFeedback.current

    var triggerCompletedSignal by remember { mutableStateOf(false) }
    LaunchedEffect(lastStatus) {
        if (lastStatus == "SUCCESS" || lastStatus == "MANUAL") {
            triggerCompletedSignal = true
            delay(1500)
            triggerCompletedSignal = false
        }
    }

    val successScale by animateFloatAsState(
        targetValue = if (triggerCompletedSignal) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "successScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "executionPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val cardBorder = when {
        isExecuting -> androidx.compose.foundation.BorderStroke(2.5.dp, themeColor.copy(alpha = pulseAlpha))
        triggerCompletedSignal -> androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF10B981))
        isSelected -> androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale * successScale)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                if (isSelectionMode) {
                    onSelectToggle()
                } else {
                    onClick()
                }
            }
            .testTag("flow_card_${flow.id}"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExecuting -> themeColor.copy(alpha = 0.25f * pulseAlpha).compositeOver(MaterialTheme.colorScheme.surface)
                triggerCompletedSignal -> Color(0xFF10B981).copy(alpha = 0.15f).compositeOver(MaterialTheme.colorScheme.surface)
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                flow.isEnabled -> themeColor.copy(alpha = 0.15f).compositeOver(MaterialTheme.colorScheme.surface)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = cardBorder,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Mode Checkbox or Icon badge
                if (isSelectionMode) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        modifier = Modifier.testTag("select_checkbox_${flow.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Icon badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFlowIconVector(flow.iconName),
                        contentDescription = flow.title,
                        tint = themeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flow.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(themeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = flow.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = themeColor
                            )
                        }

                        Text(
                            text = "• ${flow.triggerType.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isTemplate && !isSelectionMode) {
                    Switch(
                        checked = flow.isEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggle()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColor
                        ),
                        modifier = Modifier.testTag("flow_switch_${flow.id}")
                    )
                } else if (isTemplate && onActivateTemplate != null) {
                    OutlinedButton(
                        onClick = onActivateTemplate,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("activate_tpl_btn_${flow.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Activate", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activate", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = flow.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Tags pills row
            if (flow.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    flow.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Pills summary
            val actionConfig = JsonUtils.deserializeAction(flow.actionConfigJson)
            val actionSummary = FlowExecutor.generateActionSummary(actionConfig)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .border(1.dp, themeColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚡ $actionSummary",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!isTemplate) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamic Color-Coded Execution Status Badge
                    val (statusColor, statusText) = when {
                        isExecuting -> themeColor to "Executing..."
                        lastStatus == "SUCCESS" || lastStatus == "MANUAL" -> Color(0xFF10B981) to "Success"
                        lastStatus == "FAILURE" -> MaterialTheme.colorScheme.error to "Failed"
                        lastStatus == "SKIPPED" -> Color(0xFFF59E0B) to "Skipped"
                        else -> MaterialTheme.colorScheme.outline to "Not Run"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("status_badge_${flow.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isExecuting) statusColor.copy(alpha = pulseAlpha) else statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        val lastRunStr = if (flow.lastRunTimeMillis != null) {
                            val sdf = SimpleDateFormat("HH:mm, MMM d", Locale.getDefault())
                            "(${sdf.format(Date(flow.lastRunTimeMillis))})"
                        } else ""

                        Text(
                            text = lastRunStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!isSelectionMode) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_flow_btn_${flow.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Flow",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Elevated Floating Play Icon Button or Circular Progress Indicator
                            Surface(
                                onClick = {
                                    if (!isExecuting) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onRunNow()
                                    }
                                },
                                shape = CircleShape,
                                color = when {
                                    isExecuting -> themeColor.copy(alpha = 0.8f)
                                    triggerCompletedSignal -> Color(0xFF10B981)
                                    else -> themeColor
                                },
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("run_now_btn_${flow.id}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        isExecuting -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.5.dp
                                            )
                                        }
                                        triggerCompletedSignal -> {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Trigger Completed Successfully",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Test Trigger Routine Now",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
