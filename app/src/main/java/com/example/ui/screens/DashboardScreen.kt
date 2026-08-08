package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowEntity
import com.example.data.HistoryLogEntity
import com.example.ui.components.getFlowIconVector
import com.example.viewmodel.AiRoutineViewModel
import com.example.viewmodel.AiInsight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    userFlows: List<FlowEntity>,
    historyLogs: List<HistoryLogEntity>,
    aiViewModel: AiRoutineViewModel? = null,
    modifier: Modifier = Modifier
) {
    val defaultInsights = remember { emptyList<AiInsight>() }
    val aiInsightsState = aiViewModel?.aiInsights?.collectAsStateWithLifecycle(initialValue = defaultInsights)
    val aiInsights = aiInsightsState?.value ?: defaultInsights

    val isAnalyzingState = aiViewModel?.isAnalyzingInsights?.collectAsStateWithLifecycle(initialValue = false)
    val isAnalyzingInsights = isAnalyzingState?.value ?: false

    androidx.compose.runtime.LaunchedEffect(userFlows, historyLogs) {
        if (aiInsights.isEmpty()) {
            aiViewModel?.generateInsightsForHistory(userFlows, historyLogs)
        }
    }

    val now = remember { System.currentTimeMillis() }
    val thirtyDaysAgo = remember { now - (30L * 86_400_000L) }

    // Filter logs to last 30 days
    val logs30Days = remember(historyLogs) {
        historyLogs.filter { it.timestampMillis >= thirtyDaysAgo }
    }

    val totalExecutions = logs30Days.size
    val successCount = logs30Days.count { it.status == "SUCCESS" || it.status == "MANUAL" }
    val failureCount = logs30Days.count { it.status == "FAILED" || it.status == "FAILURE" }
    val skippedCount = logs30Days.count { it.status == "SKIPPED" }
    val successRate = if (totalExecutions > 0) (successCount * 100) / totalExecutions else 100

    // Group executions by day offset (0 = today, 29 = 29 days ago)
    val dailyCounts = remember(logs30Days) {
        val counts = IntArray(30) { 0 }
        logs30Days.forEach { log ->
            val daysAgo = ((now - log.timestampMillis) / 86_400_000L).toInt()
            if (daysAgo in 0..29) {
                counts[29 - daysAgo]++
            }
        }
        counts
    }

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("dashboard_screen")
        ) {
            // Screen Title Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Execution Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "30-day automation performance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "30 Days",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Automation Insights Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("automation_insights_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automation Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "AI recommendations based on history",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (isAnalyzingInsights) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                    aiViewModel?.generateInsightsForHistory(userFlows, historyLogs)
                                }
                            ) {
                                Text(
                                    text = "Re-analyze",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (aiInsights.isEmpty()) {
                        Text(
                            text = "Analyzing routine history patterns...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            aiInsights.forEach { insight ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = insight.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (insight.impact == "High") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "${insight.category} • ${insight.impact}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = insight.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Cards Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Executions",
                    value = "$totalExecutions",
                    subtitle = "Last 30 Days",
                    icon = Icons.Default.BarChart,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Success Rate",
                    value = "$successRate%",
                    subtitle = "$successCount successful",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Active Routines",
                    value = "${userFlows.count { it.isEnabled }}",
                    subtitle = "${userFlows.size} Total created",
                    icon = Icons.Default.Speed,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Daily Avg",
                    value = String.format(Locale.getDefault(), "%.1f", totalExecutions / 30.0),
                    subtitle = "Runs per day",
                    icon = Icons.Default.Schedule,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 30-Day Execution Frequency Bar Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_execution_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Routine Execution Frequency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Daily execution volume (30-day window)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Selected day tooltip info
                    if (selectedDayIndex != null) {
                        val index = selectedDayIndex!!
                        val dayOffset = 29 - index
                        val dayDateMs = now - (dayOffset * 86_400_000L)
                        val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dayDateMs))
                        val countOnDay = dailyCounts[index]

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📅 $dateStr",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$countOnDay executions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Canvas Bar Chart
                    val maxCount = remember(dailyCounts) {
                        val max = dailyCounts.maxOrNull() ?: 1
                        if (max < 4) 4 else max
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val barWidthStep = size.width / 30f
                                        val clickedIndex = (offset.x / barWidthStep).toInt().coerceIn(0, 29)
                                        selectedDayIndex = clickedIndex
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height - 30.dp.toPx() // leave room for labels
                            val barCount = 30
                            val barSpacing = 3.dp.toPx()
                            val totalSpacing = barSpacing * (barCount - 1)
                            val barWidth = (canvasWidth - totalSpacing) / barCount

                            // Draw baseline and grid lines
                            val gridColor = surfaceVariant.copy(alpha = 0.5f)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, canvasHeight),
                                end = Offset(canvasWidth, canvasHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, canvasHeight / 2f),
                                end = Offset(canvasWidth, canvasHeight / 2f),
                                strokeWidth = 0.8.dp.toPx()
                            )

                            // Render bars
                            for (i in 0 until barCount) {
                                val count = dailyCounts[i]
                                val barHeightFraction = count.toFloat() / maxCount.toFloat()
                                val barH = (canvasHeight * barHeightFraction).coerceAtLeast( if (count > 0) 8.dp.toPx() else 2.dp.toPx() )
                                val x = i * (barWidth + barSpacing)
                                val y = canvasHeight - barH

                                val isSelected = selectedDayIndex == i
                                val barColor = when {
                                    isSelected -> secondaryColor
                                    count > 0 -> primaryColor
                                    else -> surfaceVariant.copy(alpha = 0.4f)
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date range X-axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val thirtyDaysAgoStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(thirtyDaysAgo))
                        val fifteenDaysAgoStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(now - 15L * 86_400_000L))
                        val todayStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(now))

                        Text(thirtyDaysAgoStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(fifteenDaysAgoStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Today ($todayStr)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Status Distribution Chart & Legend Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Execution Outcome Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut Ring Canvas
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeW = 16.dp.toPx()
                                val radius = (size.minDimension - strokeW) / 2
                                val centerPt = Offset(size.width / 2, size.height / 2)

                                val successAngle = if (totalExecutions > 0) (successCount.toFloat() / totalExecutions) * 360f else 270f
                                val failureAngle = if (totalExecutions > 0) (failureCount.toFloat() / totalExecutions) * 360f else 0f
                                val skippedAngle = if (totalExecutions > 0) (skippedCount.toFloat() / totalExecutions) * 360f else 0f

                                // Draw Success Arc (Green)
                                drawArc(
                                    color = Color(0xFF10B981),
                                    startAngle = -90f,
                                    sweepAngle = successAngle.coerceAtLeast(2f),
                                    useCenter = false,
                                    style = Stroke(width = strokeW)
                                )

                                // Draw Failure Arc (Red/Error)
                                drawArc(
                                    color = errorColor,
                                    startAngle = -90f + successAngle,
                                    sweepAngle = failureAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeW)
                                )

                                // Draw Skipped Arc (Amber)
                                drawArc(
                                    color = Color(0xFFF59E0B),
                                    startAngle = -90f + successAngle + failureAngle,
                                    sweepAngle = skippedAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeW)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$successRate%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Success",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Outcome Legend Column
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendItem(color = Color(0xFF10B981), label = "Success", count = successCount)
                            LegendItem(color = errorColor, label = "Failed", count = failureCount)
                            LegendItem(color = Color(0xFFF59E0B), label = "Skipped", count = skippedCount)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Comparative Success vs Failure Rates Bar Chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routine_success_failure_bar_chart_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Routines Performance (Success vs Failure)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Side-by-side comparison of successful and failed runs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    val activeFlowStats = remember(logs30Days, userFlows) {
                        userFlows.map { flow ->
                            val flowLogs = logs30Days.filter { it.flowId == flow.id }
                            val successes = flowLogs.count { it.status == "SUCCESS" || it.status == "MANUAL" }
                            val failures = flowLogs.count { it.status == "FAILED" || it.status == "FAILURE" }
                            Triple(flow, successes, failures)
                        }.filter { it.second > 0 || it.third > 0 }
                         .sortedByDescending { it.second + it.third }
                         .take(5) // Limit to top 5 routines for perfect aesthetics
                    }

                    if (activeFlowStats.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No execution data available for comparison.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        var selectedFlowIndex by remember { mutableStateOf<Int?>(null) }

                        // Selected routine tooltip/details
                        if (selectedFlowIndex != null && selectedFlowIndex!! < activeFlowStats.size) {
                            val (flow, sCount, fCount) = activeFlowStats[selectedFlowIndex!!]
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = flow.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "✓ $sCount Success",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                        Text(
                                            text = "✗ $fCount Failed",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = errorColor
                                        )
                                    }
                                }
                            }
                        }

                        val maxStatValue = remember(activeFlowStats) {
                            val maxVal = activeFlowStats.flatMap { listOf(it.second, it.third) }.maxOrNull() ?: 1
                            if (maxVal < 3) 3 else maxVal
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(activeFlowStats) {
                                        detectTapGestures { offset ->
                                            val groupWidth = size.width / activeFlowStats.size.toFloat()
                                            val clickedIndex = (offset.x / groupWidth).toInt().coerceIn(0, activeFlowStats.size - 1)
                                            selectedFlowIndex = clickedIndex
                                        }
                                    }
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height - 30.dp.toPx() // leave room for labels
                                val groupCount = activeFlowStats.size
                                val groupWidth = canvasWidth / groupCount.toFloat()
                                val barWidth = 14.dp.toPx()
                                val barSpacing = 4.dp.toPx() // space between success and failure bar
                                val gridColor = surfaceVariant.copy(alpha = 0.5f)

                                // Draw baseline
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, canvasHeight),
                                    end = Offset(canvasWidth, canvasHeight),
                                    strokeWidth = 1.dp.toPx()
                                )

                                for (i in 0 until groupCount) {
                                    val (_, sCount, fCount) = activeFlowStats[i]
                                    val groupCenterX = (i * groupWidth) + (groupWidth / 2f)

                                    // Success bar (Green)
                                    val sHeightFraction = sCount.toFloat() / maxStatValue.toFloat()
                                    val sBarH = (canvasHeight * sHeightFraction).coerceAtLeast(if (sCount > 0) 8.dp.toPx() else 2.dp.toPx())
                                    val sX = groupCenterX - barWidth - (barSpacing / 2f)
                                    val sY = canvasHeight - sBarH

                                    // Failure bar (Red)
                                    val fHeightFraction = fCount.toFloat() / maxStatValue.toFloat()
                                    val fBarH = (canvasHeight * fHeightFraction).coerceAtLeast(if (fCount > 0) 8.dp.toPx() else 2.dp.toPx())
                                    val fX = groupCenterX + (barSpacing / 2f)
                                    val fY = canvasHeight - fBarH

                                    val isSelected = selectedFlowIndex == i

                                    // Draw Success Bar
                                    drawRoundRect(
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.75f),
                                        topLeft = Offset(sX, sY),
                                        size = Size(barWidth, sBarH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )

                                    // Draw Failure Bar
                                    drawRoundRect(
                                        color = if (isSelected) errorColor else errorColor.copy(alpha = 0.75f),
                                        topLeft = Offset(fX, fY),
                                        size = Size(barWidth, fBarH),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // X-Axis Labels (Shortened Title or Category of routine)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            activeFlowStats.forEachIndexed { idx, (flow, _, _) ->
                                val truncatedTitle = if (flow.title.length > 10) {
                                    flow.title.take(8) + ".."
                                } else {
                                    flow.title
                                }
                                val isSelected = selectedFlowIndex == idx
                                Text(
                                    text = truncatedTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFlowIndex = idx },
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Legend row for our new bar chart
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Success / Manual Runs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(20.dp))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(errorColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Failed Runs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Frequently Executed Routines Leaderboard
            Text(
                text = "🏆 Top Executed Automations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val flowRunCounts = remember(logs30Days, userFlows) {
                userFlows.map { flow ->
                    val runs = logs30Days.count { it.flowId == flow.id }
                    flow to runs
                }.sortedByDescending { it.second }
            }

            if (flowRunCounts.isEmpty()) {
                Text(
                    text = "No user routines created yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                flowRunCounts.take(5).forEach { (flow, runs) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getFlowIconVector(flow.iconName),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = flow.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Trigger: ${flow.triggerType.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$runs runs",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
