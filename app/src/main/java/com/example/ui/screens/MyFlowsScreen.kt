package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowEntity
import com.example.engine.SimulatedEnvironment
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.FlowCard

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.RoutineDetailDialog

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import com.example.data.HistoryLogEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyFlowsScreen(
    flows: List<FlowEntity>,
    historyLogs: List<HistoryLogEntity> = emptyList(),
    executingFlowIds: Set<String> = emptySet(),
    environment: SimulatedEnvironment,
    selectedCategory: String,
    searchQuery: String,
    recentSearches: List<String> = emptyList(),
    onAddRecentSearch: (String) -> Unit = {},
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleFlow: (FlowEntity) -> Unit,
    onRunFlowNow: (FlowEntity) -> Unit,
    onDeleteFlow: (FlowEntity) -> Unit,
    onBatchDeleteFlows: (Set<String>) -> Unit = {},
    onBatchUpdateEnabled: (Set<String>, Boolean) -> Unit = { _, _ -> },
    onEditFlow: (FlowEntity) -> Unit,
    onCreateNewFlow: () -> Unit,
    onNavigateToDiscover: () -> Unit,
    onOpenEnvironmentSimulator: () -> Unit,
    onOpenAiDialog: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedFlowForDetail by remember { mutableStateOf<FlowEntity?>(null) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedFlowIds by remember { mutableStateOf(setOf<String>()) }
    var selectedTag by remember { mutableStateOf("All") }

    val haptic = LocalHapticFeedback.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columnCount = if (screenWidthDp >= 600) 2 else 1

    // Extract unique tags from all user routines
    val allTags = remember(flows) {
        val tags = mutableSetOf("All")
        flows.forEach { flow ->
            flow.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tags.add(it) }
        }
        tags.toList()
    }

    // Filter flows by tag
    val tagFilteredFlows = remember(flows, selectedTag) {
        if (selectedTag == "All") flows
        else flows.filter { it.tags.contains(selectedTag, ignoreCase = true) }
    }

    // Map flowId to last execution status
    val lastStatusMap = remember(historyLogs) {
        historyLogs.groupBy { it.flowId }
            .mapValues { entry -> entry.value.maxByOrNull { it.timestampMillis }?.status }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
        ) {
            // Hero Monitoring Status Banner
            item {
                HeroStatusBanner(
                    activeCount = flows.count { it.isEnabled },
                    totalCount = flows.size,
                    environment = environment,
                    onOpenSimulator = onOpenEnvironmentSimulator,
                    onOpenAiDialog = onOpenAiDialog
                )
            }

            // Selection Mode Batch Control Bar or Search & Filters
            item {
                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                ) {
                    // Batch Selection Header
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("batch_selection_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedFlowIds.size} of ${tagFilteredFlows.size} selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Row {
                                    TextButton(onClick = {
                                        selectedFlowIds = if (selectedFlowIds.size == tagFilteredFlows.size) emptySet()
                                        else tagFilteredFlows.map { it.id }.toSet()
                                    }) {
                                        Text(if (selectedFlowIds.size == tagFilteredFlows.size) "Deselect All" else "Select All")
                                    }
                                    IconButton(onClick = {
                                        isSelectionMode = false
                                        selectedFlowIds = emptySet()
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Exit Selection")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Batch Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onBatchUpdateEnabled(selectedFlowIds, false)
                                        isSelectionMode = false
                                        selectedFlowIds = emptySet()
                                    },
                                    enabled = selectedFlowIds.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("batch_pause_btn")
                                ) {
                                    Text("Pause", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        onBatchUpdateEnabled(selectedFlowIds, true)
                                        isSelectionMode = false
                                        selectedFlowIds = emptySet()
                                    },
                                    enabled = selectedFlowIds.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("batch_resume_btn")
                                ) {
                                    Text("Resume", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        onBatchDeleteFlows(selectedFlowIds)
                                        isSelectionMode = false
                                        selectedFlowIds = emptySet()
                                    },
                                    enabled = selectedFlowIds.isNotEmpty(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.weight(1.2f).testTag("batch_delete_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !isSelectionMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column {
                        // Search Bar & Multi-Select Toggle Button Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    onSearchQueryChanged(it)
                                    if (it.isNotBlank()) onAddRecentSearch(it)
                                },
                                placeholder = { Text("Search by name, category, or trigger...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { onSearchQueryChanged("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true,
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("search_flows_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Selection Mode Toggle Button
                            FilledTonalIconButton(
                                onClick = {
                                    isSelectionMode = true
                                    selectedFlowIds = emptySet()
                                },
                                modifier = Modifier.testTag("enable_selection_mode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Select Multiple",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Recent Searches Chips Row
                        if (recentSearches.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Recent:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(recentSearches) { term ->
                                        androidx.compose.material3.FilterChip(
                                            selected = searchQuery == term,
                                            onClick = {
                                                onSearchQueryChanged(if (searchQuery == term) "" else term)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            label = { Text(term, style = MaterialTheme.typography.labelSmall) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("recent_search_chip_$term")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CategoryChipRow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = onCategorySelected
                        )

                        // Tag Filter Chips Row
                        if (allTags.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tags: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                androidx.compose.foundation.lazy.LazyRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(allTags) { tag ->
                                        val isSelectedTag = tag == selectedTag
                                        androidx.compose.material3.FilterChip(
                                            selected = isSelectedTag,
                                            onClick = { selectedTag = tag },
                                            label = { Text("#$tag", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("tag_chip_$tag")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading skeleton state or empty state or routines list
            if (isLoading) {
                items(3) {
                    RoutineSkeletonCard()
                }
            } else if (tagFilteredFlows.isEmpty()) {
                item {
                    EmptyFlowsView(
                        searchQuery = searchQuery,
                        onCreateNew = onCreateNewFlow,
                        onExploreTemplates = onNavigateToDiscover
                    )
                }
            } else {
                if (columnCount == 1) {
                    items(tagFilteredFlows, key = { it.id }) { flow ->
                        val isSelected = selectedFlowIds.contains(flow.id)
                        val lastStatus = lastStatusMap[flow.id]

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    onDeleteFlow(flow)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                val color = if (direction == SwipeToDismissBoxValue.EndToStart) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(color),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (direction == SwipeToDismissBoxValue.EndToStart) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Flow",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(end = 24.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("swipe_to_dismiss_box_${flow.id}")
                        ) {
                            FlowCard(
                                flow = flow,
                                onToggle = { onToggleFlow(flow) },
                                onRunNow = { onRunFlowNow(flow) },
                                onDelete = { onDeleteFlow(flow) },
                                onClick = {
                                    if (isSelectionMode) {
                                        selectedFlowIds = if (isSelected) selectedFlowIds - flow.id else selectedFlowIds + flow.id
                                    } else {
                                        selectedFlowForDetail = flow
                                    }
                                },
                                lastStatus = lastStatus,
                                isSelectionMode = isSelectionMode,
                                isSelected = isSelected,
                                onSelectToggle = {
                                    selectedFlowIds = if (isSelected) selectedFlowIds - flow.id else selectedFlowIds + flow.id
                                },
                                modifier = Modifier
                            )
                        }
                    }
                } else {
                    val chunked = tagFilteredFlows.chunked(columnCount)
                    items(chunked) { rowFlows ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowFlows.forEach { flow ->
                                Box(modifier = Modifier.weight(1f)) {
                                    val isSelected = selectedFlowIds.contains(flow.id)
                                    val lastStatus = lastStatusMap[flow.id]
                                    FlowCard(
                                        flow = flow,
                                        onToggle = { onToggleFlow(flow) },
                                        onRunNow = { onRunFlowNow(flow) },
                                        onDelete = { onDeleteFlow(flow) },
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedFlowIds = if (isSelected) selectedFlowIds - flow.id else selectedFlowIds + flow.id
                                            } else {
                                                selectedFlowForDetail = flow
                                            }
                                        },
                                        lastStatus = lastStatus,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSelected,
                                        onSelectToggle = {
                                            selectedFlowIds = if (isSelected) selectedFlowIds - flow.id else selectedFlowIds + flow.id
                                        },
                                        modifier = Modifier
                                    )
                                }
                            }
                            if (rowFlows.size < columnCount) {
                                repeat(columnCount - rowFlows.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to create new Flow
        FloatingActionButton(
            onClick = onCreateNewFlow,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("create_flow_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Flow", modifier = Modifier.size(28.dp))
        }

        // Clickable Item Detail Dialog
        selectedFlowForDetail?.let { currentFlow ->
            // Re-fetch flow state in case toggled or edited
            val activeFlow = flows.find { it.id == currentFlow.id } ?: currentFlow
            RoutineDetailDialog(
                flow = activeFlow,
                historyLogs = historyLogs,
                onDismiss = { selectedFlowForDetail = null },
                onToggle = { onToggleFlow(activeFlow) },
                onRunNow = { onRunFlowNow(activeFlow) },
                onEdit = {
                    selectedFlowForDetail = null
                    onEditFlow(activeFlow)
                }
            )
        }
    }
}

@Composable
fun HeroStatusBanner(
    activeCount: Int,
    totalCount: Int,
    environment: SimulatedEnvironment,
    onOpenSimulator: () -> Unit,
    onOpenAiDialog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .testTag("hero_status_banner"),
        colors = CardDefaults.cardColors(
            containerColor = Color.Unspecified
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SwAIft Engine Active",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$activeCount of $totalCount routines monitoring",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenAiDialog,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_ai_assistant_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Create", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated context live pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ${environment.locationLabel} • 📶 ${environment.wifiSsid} • 🔋 ${environment.batteryPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    FilledTonalButton(
                        onClick = onOpenSimulator,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_simulator_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun EmptyRoutinesIllustration() {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    androidx.compose.foundation.Canvas(
        modifier = Modifier.size(100.dp)
    ) {
        val width = size.width
        val height = size.height
        drawCircle(
            color = surfaceVariant.copy(alpha = 0.6f),
            radius = width / 2f
        )
        drawCircle(
            color = primaryColor.copy(alpha = 0.25f),
            radius = width / 3f
        )
        drawCircle(
            color = primaryColor,
            radius = width / 6f
        )
    }
}

@Composable
fun EmptyFlowsView(
    searchQuery: String,
    onCreateNew: () -> Unit,
    onExploreTemplates: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyRoutinesIllustration()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isBlank()) "No Flows Created Yet" else "No matching flows found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (searchQuery.isBlank())
                "Automate your daily phone habits in seconds with SwAIft Q&A setup or pre-built gallery templates."
            else
                "Try searching for something else or clear the category filter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onCreateNew,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("empty_create_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Flow")
            }
            FilledTonalButton(
                onClick = onExploreTemplates,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("empty_explore_btn")
            ) {
                Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Explore Gallery")
            }
        }
    }
}
