@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package ru.custle.mobile.feature.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectNodeDto

// ── Dark-friendly accent tones ──
private val BlueDark = Color(0xFF1A2A4D)
private val BlueText = Color(0xFF8DB0F0)
private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)
private val AmberDark = Color(0xFF3D2E0A)
private val AmberText = Color(0xFFE8C060)
private val RedDark = Color(0xFF3D1515)
private val RedText = Color(0xFFE88080)

private enum class StatusFilter(val label: String, val keys: Set<String>) {
    ALL("Все", emptySet()),
    IN_PROGRESS("В работе", setOf("in_progress", "в работе")),
    COMPLETED("Завершён", setOf("completed", "завершён", "завершен")),
    ON_HOLD("На паузе", setOf("on_hold", "на паузе", "приостановлен")),
    NOT_STARTED("Не начат", setOf("not_started", "не начат")),
}

@Composable
fun ProjectsScreen(
    tree: List<ObjectNodeDto>,
    onOpenObject: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(StatusFilter.ALL) }

    // Navigation stack for drill-down
    val navStack = remember { mutableStateListOf<ObjectNodeDto>() }

    // Current level nodes
    val currentNodes = if (navStack.isEmpty()) tree else navStack.last().children

    // Search results (flat with paths)
    val searchResults = if (query.isNotBlank()) flatSearch(tree, query.trim()) else emptyList()

    // Filtered nodes for current level
    val displayNodes = when {
        query.isNotBlank() -> emptyList() // use searchResults instead
        statusFilter != StatusFilter.ALL -> currentNodes.filter { node ->
            node.status?.lowercase() in statusFilter.keys
        }
        else -> currentNodes
    }

    val totalNodes = countNodes(tree)
    val isSearching = query.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Header ──
        ProjectsHeader(
            totalNodes = totalNodes,
            currentCount = currentNodes.size,
            navStack = navStack,
            onNavigateUp = { if (navStack.isNotEmpty()) navStack.removeLast() },
            onNavigateTo = { index ->
                // Navigate to specific breadcrumb level
                while (navStack.size > index + 1) navStack.removeLast()
            },
            onNavigateRoot = { navStack.clear() },
        )

        // ── Search ──
        SearchBar(
            query = query,
            onQueryChange = { query = it },
        )

        // ── Status filter chips ──
        if (!isSearching) {
            StatusFilterRow(
                selected = statusFilter,
                onSelect = { statusFilter = it },
            )
        }

        // ── Content ──
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // Current folder info card (when drilled in)
            if (navStack.isNotEmpty() && !isSearching) {
                item(key = "__folder_card__") {
                    CurrentFolderCard(
                        node = navStack.last(),
                        onOpenDetail = { onOpenObject(navStack.last().id) },
                    )
                }
            }

            if (isSearching) {
                if (searchResults.isEmpty()) {
                    item(key = "__empty__") { EmptyState(query = query) }
                } else {
                    items(searchResults, key = { it.first.id }) { (node, path) ->
                        SearchResultRow(
                            node = node,
                            path = path,
                            onTap = { onOpenObject(node.id) },
                        )
                    }
                }
            } else if (displayNodes.isEmpty()) {
                item(key = "__empty__") { EmptyState(query = query, hasFilter = statusFilter != StatusFilter.ALL) }
            } else {
                items(displayNodes, key = { it.id }) { node ->
                    NodeRow(
                        node = node,
                        onTap = {
                            if (node.children.isNotEmpty()) {
                                navStack.add(node)
                            } else {
                                onOpenObject(node.id)
                            }
                        },
                        onLongTap = { onOpenObject(node.id) },
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Header with breadcrumbs
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProjectsHeader(
    totalNodes: Int,
    currentCount: Int,
    navStack: List<ObjectNodeDto>,
    onNavigateUp: () -> Unit,
    onNavigateTo: (Int) -> Unit,
    onNavigateRoot: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Title row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (navStack.isNotEmpty()) 4.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navStack.isNotEmpty()) {
                IconButton(onClick = onNavigateUp, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack, "Назад",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
            }

            Text(
                "Проекты",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            // Counters
            Text(
                "$currentCount из $totalNodes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Breadcrumbs
        if (navStack.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Root
                Text(
                    "Все",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onNavigateRoot)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )

                navStack.forEachIndexed { index, node ->
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp),
                    )
                    val isLast = index == navStack.lastIndex
                    Text(
                        node.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isLast) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (!isLast) Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onNavigateTo(index) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        else Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ═══════════════════════════════════════════════════════════════
// Search
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        placeholder = { Text("Поиск по названию, типу, ответственному") },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, "Очистить", modifier = Modifier.size(18.dp))
                }
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
    )
}

// ═══════════════════════════════════════════════════════════════
// Status Filter Chips
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatusFilterRow(
    selected: StatusFilter,
    onSelect: (StatusFilter) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(StatusFilter.entries.toList()) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Current Folder Card (shown when drilled into a node)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CurrentFolderCard(
    node: ObjectNodeDto,
    onOpenDetail: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onOpenDetail,
            ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconColor = parseTypeColor(node.typeColor)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(kindIcon(node.typeKind), null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    node.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    node.typeName,
                    node.status,
                    "${node.children.size} объектов внутри",
                ).joinToString(" \u00B7 ")
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (node.progress > 0) {
                ProgressPill(node.progress)
            }
        }
    }
    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

// ═══════════════════════════════════════════════════════════════
// Node Row (single level item)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun NodeRow(
    node: ObjectNodeDto,
    onTap: () -> Unit,
    onLongTap: () -> Unit,
) {
    val hasChildren = node.children.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onTap,
                onLongClick = onLongTap,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Type icon
        val iconColor = parseTypeColor(node.typeColor)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(kindIcon(node.typeKind), null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        // Name + meta
        Column(modifier = Modifier.weight(1f)) {
            Text(
                node.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                node.typeName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                node.assigneeName?.let { name ->
                    Text("\u00B7", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    AssigneeAvatar(name)
                    Text(
                        name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }

        // Right side
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (node.progress > 0) {
                ProgressPill(node.progress)
            }

            node.status?.takeIf { it.isNotBlank() }?.let { status ->
                StatusBadge(status)
            }

            if (hasChildren) {
                // Children count + chevron
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            node.children.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

// ═══════════════════════════════════════════════════════════════
// Search Result Row (flat, with path)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchResultRow(
    node: ObjectNodeDto,
    path: String,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onTap,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val iconColor = parseTypeColor(node.typeColor)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(kindIcon(node.typeKind), null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                node.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (path.isNotBlank()) {
                Text(
                    path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        node.status?.takeIf { it.isNotBlank() }?.let { status ->
            Spacer(Modifier.width(8.dp))
            StatusBadge(status)
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
}

// ═══════════════════════════════════════════════════════════════
// Progress Pill
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProgressPill(progress: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = EmeraldDark,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .width(24.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = EmeraldText,
                trackColor = EmeraldDark,
                strokeCap = StrokeCap.Round,
            )
            Text(
                "$progress%",
                style = MaterialTheme.typography.labelSmall,
                color = EmeraldText,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Status Badge
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "in_progress", "в работе" -> BlueDark to BlueText
        "completed", "завершён", "завершен" -> EmeraldDark to EmeraldText
        "on_hold", "на паузе", "приостановлен" -> AmberDark to AmberText
        "cancelled", "отменён", "отменен" -> RedDark to RedText
        "not_started", "не начат" ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = text,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Assignee Avatar
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AssigneeAvatar(name: String) {
    val letter = name.firstOrNull()?.uppercase() ?: "?"
    Surface(
        shape = CircleShape,
        color = BlueDark,
        modifier = Modifier.size(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                letter,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f),
                fontWeight = FontWeight.SemiBold,
                color = BlueText,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Empty State
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(query: String, hasFilter: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Rounded.FolderOpen, null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(32.dp),
        )
        Text(
            when {
                query.isNotBlank() -> "Ничего не найдено"
                hasFilter -> "Нет объектов с таким статусом"
                else -> "Здесь пока пусто"
            },
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (query.isNotBlank()) {
            Text(
                "Попробуйте изменить запрос",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Utilities
// ═══════════════════════════════════════════════════════════════

private fun kindIcon(typeKind: String?): ImageVector = when (typeKind?.lowercase()) {
    "directory" -> Icons.Rounded.Folder
    "project" -> Icons.Rounded.Adjust
    "task" -> Icons.Rounded.CheckBox
    "document" -> Icons.Rounded.Description
    else -> Icons.Rounded.FolderOpen
}

private fun parseTypeColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF8DB0F0)
    return try {
        val clean = hex.trimStart('#')
        Color(("FF$clean").toLong(16))
    } catch (_: Exception) {
        Color(0xFF8DB0F0)
    }
}

/** Flatten tree for search — returns (node, breadcrumb path) pairs */
private fun flatSearch(
    nodes: List<ObjectNodeDto>,
    query: String,
    parentPath: String = "",
): List<Pair<ObjectNodeDto, String>> {
    val key = query.lowercase()
    val results = mutableListOf<Pair<ObjectNodeDto, String>>()
    for (node in nodes) {
        val currentPath = if (parentPath.isEmpty()) node.name else "$parentPath / ${node.name}"
        val matches = node.name.lowercase().contains(key) ||
            (node.typeName?.lowercase()?.contains(key) == true) ||
            (node.assigneeName?.lowercase()?.contains(key) == true)
        if (matches) {
            results.add(node to parentPath)
        }
        results.addAll(flatSearch(node.children, query, currentPath))
    }
    return results
}

private fun countNodes(nodes: List<ObjectNodeDto>): Int =
    nodes.sumOf { 1 + countNodes(it.children) }

private fun countActiveNodes(nodes: List<ObjectNodeDto>): Int =
    nodes.sumOf { node ->
        val isActive = node.status?.isNotBlank() == true || node.progress > 0 || node.children.isNotEmpty()
        (if (isActive) 1 else 0) + countActiveNodes(node.children)
    }
