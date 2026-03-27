@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package ru.custle.mobile.feature.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

// ── Dark-friendly accent tones (matching dashboard) ──
private val BlueDark = Color(0xFF1A2A4D)
private val BlueText = Color(0xFF8DB0F0)
private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)
private val AmberDark = Color(0xFF3D2E0A)
private val AmberText = Color(0xFFE8C060)
private val RedDark = Color(0xFF3D1515)
private val RedText = Color(0xFFE88080)

@Composable
fun ProjectsScreen(
    tree: List<ObjectNodeDto>,
    onOpenObject: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val visibleTree = if (query.isBlank()) tree else filterTree(tree, query.trim())
    val totalNodes = countNodes(tree)
    val visibleNodes = countNodes(visibleTree)
    val activeCount = countActiveNodes(tree)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Header ──
        item {
            ProjectsHeader(
                totalNodes = totalNodes,
                activeCount = activeCount,
                rootCount = tree.size,
            )
        }

        // ── Search ──
        item {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                visibleNodes = visibleNodes,
                totalNodes = totalNodes,
            )
        }

        // ── Tree ──
        if (visibleTree.isEmpty()) {
            item { EmptyState(query = query) }
        } else {
            treeNodes(visibleTree, 0, onOpenObject)
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Header
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ProjectsHeader(
    totalNodes: Int,
    activeCount: Int,
    rootCount: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            "Проекты",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            "Структура объектов workspace",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricChip(Modifier.weight(1f), totalNodes.toString(), "Всего", BlueDark, BlueText)
            MetricChip(Modifier.weight(1f), activeCount.toString(), "Активных", EmeraldDark, EmeraldText)
            MetricChip(Modifier.weight(1f), rootCount.toString(), "Корневых", AmberDark, AmberText)
        }
    }

    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun MetricChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    bg: Color,
    accent: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = 0.7f),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Search
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    visibleNodes: Int,
    totalNodes: Int,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Найти объект, тип или ответственного") },
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
        if (query.isNotBlank()) {
            Text(
                "Найдено $visibleNodes из $totalNodes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Tree
// ═══════════════════════════════════════════════════════════════

private fun androidx.compose.foundation.lazy.LazyListScope.treeNodes(
    nodes: List<ObjectNodeDto>,
    depth: Int,
    onOpenObject: (String) -> Unit,
) {
    items(nodes, key = { it.id }) { node ->
        TreeRow(node = node, depth = depth, onOpenObject = onOpenObject)
    }
    nodes.forEach { node ->
        if (node.children.isNotEmpty()) {
            treeNodes(node.children, depth + 1, onOpenObject)
        }
    }
}

@Composable
private fun TreeRow(
    node: ObjectNodeDto,
    depth: Int,
    onOpenObject: (String) -> Unit,
) {
    val indentDp = (depth * 24 + 12).dp
    val kindIcon = when (node.typeKind?.lowercase()) {
        "directory" -> Icons.Rounded.Folder
        "project" -> Icons.Rounded.Adjust
        "task" -> Icons.Rounded.CheckBox
        "document" -> Icons.Rounded.Description
        else -> Icons.Rounded.FolderOpen
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onOpenObject(node.id) },
            )
            .padding(start = indentDp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Type icon with tinted background
        val iconColor = parseTypeColor(node.typeColor)
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = iconColor.copy(alpha = 0.15f),
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(kindIcon, null, tint = iconColor, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(Modifier.width(10.dp))

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

            // Meta row: type · assignee
            val metaParts = mutableListOf<String>()
            node.typeName?.let { metaParts.add(it) }
            node.assigneeName?.let { metaParts.add(it) }
            if (metaParts.isNotEmpty()) {
                Text(
                    metaParts.joinToString(" \u00B7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Right side: progress + status + children count
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Progress bar (if > 0)
            if (node.progress > 0) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.width(36.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { node.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = EmeraldText,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        "${node.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Status badge
            node.status?.takeIf { it.isNotBlank() }?.let { status ->
                StatusBadge(status)
            }

            // Assignee avatar
            node.assigneeName?.takeIf { it.isNotBlank() }?.let { name ->
                AssigneeAvatar(name = name)
            }
        }
    }

    // Thin divider
    HorizontalDivider(
        modifier = Modifier.padding(start = indentDp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    )
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
        modifier = Modifier.size(22.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                letter,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = BlueText,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Parse type color from hex
// ═══════════════════════════════════════════════════════════════

private fun parseTypeColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color(0xFF8DB0F0) // default blue
    return try {
        val clean = hex.trimStart('#')
        Color(("FF$clean").toLong(16))
    } catch (_: Exception) {
        Color(0xFF8DB0F0)
    }
}

// ═══════════════════════════════════════════════════════════════
// Empty State
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyState(query: String) {
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
            if (query.isBlank()) "Дерево объектов пока пустое" else "Ничего не найдено",
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
// Utility functions
// ═══════════════════════════════════════════════════════════════

private fun filterTree(nodes: List<ObjectNodeDto>, query: String): List<ObjectNodeDto> {
    val key = query.lowercase()
    return nodes.mapNotNull { node ->
        val filteredChildren = filterTree(node.children, query)
        val selfMatches = node.name.lowercase().contains(key) ||
            (node.typeName?.lowercase()?.contains(key) == true) ||
            (node.assigneeName?.lowercase()?.contains(key) == true)
        if (selfMatches || filteredChildren.isNotEmpty()) {
            node.copy(children = filteredChildren)
        } else {
            null
        }
    }
}

private fun countNodes(nodes: List<ObjectNodeDto>): Int =
    nodes.sumOf { 1 + countNodes(it.children) }

private fun countActiveNodes(nodes: List<ObjectNodeDto>): Int =
    nodes.sumOf { node ->
        val isActive = node.status?.isNotBlank() == true || node.progress > 0 || node.children.isNotEmpty()
        (if (isActive) 1 else 0) + countActiveNodes(node.children)
    }
