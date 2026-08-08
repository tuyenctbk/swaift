package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowEntity
import com.example.ui.components.CategoryChipRow
import com.example.ui.components.FlowCard

@Composable
fun DiscoverScreen(
    templates: List<FlowEntity>,
    userFlows: List<FlowEntity>,
    selectedCategory: String,
    searchQuery: String,
    recentSearches: List<String> = emptyList(),
    onAddRecentSearch: (String) -> Unit = {},
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleTemplate: (FlowEntity, Boolean) -> Unit,
    onRunTemplate: (FlowEntity) -> Unit = {},
    onEditTemplate: (FlowEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val columnCount = if (screenWidthDp >= 600) 2 else 1

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
        // 30-Second Onboarding Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .testTag("discover_onboarding_banner"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "30-Second Automation Setup",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pick a pre-configured template below and toggle on.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChanged(it)
                    if (it.isNotBlank()) onAddRecentSearch(it)
                },
                placeholder = { Text("Search routine gallery...", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_gallery_input")
            )

            if (recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Recent:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        items(recentSearches) { term ->
                            androidx.compose.material3.FilterChip(
                                selected = searchQuery == term,
                                onClick = {
                                    onSearchQueryChanged(if (searchQuery == term) "" else term)
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                },
                                label = { Text(term, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(12.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Gallery Templates
        if (columnCount == 1) {
            items(templates, key = { it.id }) { template ->
                val matchingUserFlow = userFlows.find { it.title.equals(template.title, ignoreCase = true) }
                val isEnabled = matchingUserFlow?.isEnabled == true
                FlowCard(
                    flow = template.copy(isEnabled = isEnabled, id = matchingUserFlow?.id ?: template.id),
                    onToggle = { onToggleTemplate(template, !isEnabled) },
                    onRunNow = { onRunTemplate(template) },
                    onDelete = {},
                    onClick = { onEditTemplate(template) },
                    isTemplate = false,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else {
            val chunked = templates.chunked(columnCount)
            items(chunked) { rowTemplates ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    rowTemplates.forEach { template ->
                        val matchingUserFlow = userFlows.find { it.title.equals(template.title, ignoreCase = true) }
                        val isEnabled = matchingUserFlow?.isEnabled == true
                        Box(modifier = Modifier.weight(1f)) {
                            FlowCard(
                                flow = template.copy(isEnabled = isEnabled, id = matchingUserFlow?.id ?: template.id),
                                onToggle = { onToggleTemplate(template, !isEnabled) },
                                onRunNow = { onRunTemplate(template) },
                                onDelete = {},
                                onClick = { onEditTemplate(template) },
                                isTemplate = false,
                                modifier = Modifier
                            )
                        }
                    }
                    if (rowTemplates.size < columnCount) {
                        repeat(columnCount - rowTemplates.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
}
