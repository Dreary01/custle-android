package ru.custle.mobile.feature.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.theme.Brick300
import ru.custle.mobile.core.ui.theme.Green900
import ru.custle.mobile.core.ui.theme.Sand050
import ru.custle.mobile.core.ui.theme.Sand100
import ru.custle.mobile.core.ui.theme.Sand200
import ru.custle.mobile.core.ui.theme.Sand700

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
    val rootCount = tree.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProjectsHero(
                totalNodes = totalNodes,
                activeCount = activeCount,
                rootCount = rootCount,
            )
        }
        item {
            SearchPanel(
                query = query,
                onQueryChange = { query = it },
                visibleNodes = visibleNodes,
                totalNodes = totalNodes,
            )
        }
        if (tree.isNotEmpty()) {
            item {
                ProjectsExecutiveStrip(
                    rootCount = rootCount,
                    visibleNodes = visibleNodes,
                    activeCount = activeCount,
                )
            }
        }
        if (query.isBlank() && tree.isNotEmpty()) {
            item {
                FeaturedRootsPanel(
                    roots = tree.take(3),
                    onOpenObject = onOpenObject,
                )
            }
        }
        item {
            TreePanelHeader(
                title = if (query.isBlank()) "Структура объектов" else "Результат поиска по структуре",
                hint = if (query.isBlank()) {
                    "Основное дерево workspace. Здесь важно быстро считать уровень, статус и ответственного."
                } else {
                    "Показываем только ту часть дерева, которая совпала с запросом или содержит совпавшие узлы."
                },
            )
        }
        if (visibleTree.isEmpty()) {
            item {
                EmptyProjectsState(query = query)
            }
        } else {
            treeNodes(visibleTree, 0, onOpenObject)
        }
    }
}

@Composable
private fun ProjectsHero(
    totalNodes: Int,
    activeCount: Int,
    rootCount: Int,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Sand050),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Sand200.copy(alpha = 0.22f)),
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Green900,
                                    Green900.copy(alpha = 0.95f),
                                    Sand700,
                                ),
                            ),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Sand050.copy(alpha = 0.12f),
                    ) {
                        Text(
                            "PROJECT HUB",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Sand100,
                        )
                    }
                    Text(
                        "Проекты\nи объекты",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Sand050,
                    )
                    Text(
                        "Главный вход в живую структуру workspace: дерево, владельцы, статусы и переход к карточкам объектов.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sand200,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = totalNodes.toString(),
                    label = "Всего узлов",
                    accent = Sand200,
                )
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = activeCount.toString(),
                    label = "Активных",
                    accent = Green900.copy(alpha = 0.12f),
                )
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = rootCount.toString(),
                    label = "Корней",
                    accent = Brick300.copy(alpha = 0.45f),
                )
            }
        }
    }
}

@Composable
private fun HeroMetricCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    visibleNodes: Int,
    totalNodes: Int,
) {
    ProjectsPanel(
        marker = "SEARCH",
        title = "Поиск по структуре",
        hint = "Работает по названию объекта, типу и ответственному.",
        accent = Green900.copy(alpha = 0.08f),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text("Найти объект, тип или ответственного") },
        )
        Text(
            if (query.isBlank()) "Показано $totalNodes из $totalNodes узлов"
            else "Показано $visibleNodes из $totalNodes узлов",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProjectsExecutiveStrip(
    rootCount: Int,
    visibleNodes: Int,
    activeCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExecutivePill(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.AccountTree,
            label = "Контур",
            value = "$rootCount корн.",
        )
        ExecutivePill(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.FolderCopy,
            label = "В выдаче",
            value = "$visibleNodes узл.",
        )
        ExecutivePill(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            label = "Активность",
            value = "$activeCount акт.",
        )
    }
}

@Composable
private fun ExecutivePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(listOf(Sand100, Sand050)),
                    shape = RoundedCornerShape(999.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = Green900)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FeaturedRootsPanel(
    roots: List<ObjectNodeDto>,
    onOpenObject: (String) -> Unit,
) {
    ProjectsPanel(
        marker = "PRIMARY PORTFOLIO",
        title = "Ключевые корневые объекты",
        hint = "Самые верхние узлы структуры, откуда обычно начинается переход в рабочий поток.",
        accent = Green900.copy(alpha = 0.10f),
    ) {
        roots.forEach { root ->
            FeaturedRootCard(root = root, onOpenObject = onOpenObject)
        }
    }
}

@Composable
private fun FeaturedRootCard(
    root: ObjectNodeDto,
    onOpenObject: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenObject(root.id) },
        colors = CardDefaults.elevatedCardColors(containerColor = Sand050),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Green900,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Source, contentDescription = null, tint = Sand050)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("ROOT OBJECT", style = MaterialTheme.typography.labelLarge, color = Green900)
                Text(root.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                val meta = listOfNotNull(root.typeName, root.status, root.assigneeName).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text("Открыть", style = MaterialTheme.typography.labelLarge, color = Green900)
        }
    }
}

@Composable
private fun TreePanelHeader(
    title: String,
    hint: String,
) {
    ProjectsPanel(
        marker = "TREE VIEW",
        title = title,
        hint = hint,
        accent = Green900.copy(alpha = 0.06f),
    ) {}
}

@Composable
private fun EmptyProjectsState(
    query: String,
) {
    EmptyStateCard(
        title = "Ничего не найдено",
        message = if (query.isBlank()) "Дерево объектов пока пустое."
        else "Попробуй изменить запрос. Поиск работает по названию, типу и ответственному.",
    )
}

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

private fun androidx.compose.foundation.lazy.LazyListScope.treeNodes(
    nodes: List<ObjectNodeDto>,
    depth: Int,
    onOpenObject: (String) -> Unit,
) {
    items(nodes, key = { it.id }) { node ->
        ProjectRow(node = node, depth = depth, onOpenObject = onOpenObject)
    }
    nodes.forEach { node ->
        treeNodes(node.children, depth + 1, onOpenObject)
    }
}

@Composable
private fun ProjectRow(
    node: ObjectNodeDto,
    depth: Int,
    onOpenObject: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenObject(node.id) },
        colors = CardDefaults.elevatedCardColors(containerColor = Sand050),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            HierarchyRail(depth = depth)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Green900.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = "LEVEL ${depth + 1}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green900,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            node.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val meta = listOfNotNull(node.typeName, node.status, node.assigneeName).joinToString(" • ")
                        if (meta.isNotBlank()) {
                            Text(
                                meta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (node.children.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Sand200,
                        ) {
                            Text(
                                text = "${node.children.size} влож.",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Green900,
                            )
                        }
                    }
                }
                val dates = listOfNotNull(node.planStartDate, node.planEndDate).joinToString(" -> ")
                if (dates.isNotBlank()) {
                    Text(
                        dates,
                        style = MaterialTheme.typography.bodySmall,
                        color = Sand700,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Sand100,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = Green900, modifier = Modifier.size(14.dp))
                        Text("Открыть карточку", style = MaterialTheme.typography.labelLarge, color = Green900)
                    }
                }
            }
        }
    }
}

@Composable
private fun HierarchyRail(depth: Int) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(depth.coerceAtMost(4)) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Sand200),
            )
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Green900),
        )
    }
}

@Composable
private fun ProjectsPanel(
    marker: String,
    title: String,
    hint: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Sand050,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.95f),
                            Sand050,
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Green900.copy(alpha = 0.10f),
                ) {
                    Text(
                        marker,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green900,
                    )
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}
