@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package ru.custle.mobile.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.DashboardItemDto
import ru.custle.mobile.core.ui.components.ErrorBanner
import ru.custle.mobile.core.ui.theme.Amber050
import ru.custle.mobile.core.ui.theme.Amber600
import ru.custle.mobile.core.ui.theme.Amber100
import ru.custle.mobile.core.ui.theme.DarkNav
import ru.custle.mobile.core.ui.theme.Emerald050
import ru.custle.mobile.core.ui.theme.Emerald100
import ru.custle.mobile.core.ui.theme.Emerald600
import ru.custle.mobile.core.ui.theme.Gray050
import ru.custle.mobile.core.ui.theme.Gray100
import ru.custle.mobile.core.ui.theme.Gray200
import ru.custle.mobile.core.ui.theme.Gray300
import ru.custle.mobile.core.ui.theme.Gray400
import ru.custle.mobile.core.ui.theme.Gray500
import ru.custle.mobile.core.ui.theme.Gray600
import ru.custle.mobile.core.ui.theme.Gray800
import ru.custle.mobile.core.ui.theme.Primary050
import ru.custle.mobile.core.ui.theme.Primary100
import ru.custle.mobile.core.ui.theme.Primary300
import ru.custle.mobile.core.ui.theme.Primary500
import ru.custle.mobile.core.ui.theme.Primary600
import ru.custle.mobile.core.ui.theme.Red050
import ru.custle.mobile.core.ui.theme.Red600
import ru.custle.mobile.core.ui.theme.Rose050
import ru.custle.mobile.core.ui.theme.Rose600
import ru.custle.mobile.core.ui.theme.Violet050
import ru.custle.mobile.core.ui.theme.Violet600
import ru.custle.mobile.navigation.CustleUiState
import ru.custle.mobile.navigation.MainSection
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    state: CustleUiState,
    onRefresh: () -> Unit,
    onOpenObject: (String) -> Unit,
    onOpenSection: (MainSection) -> Unit,
    onLogout: () -> Unit,
) {
    val displayName = listOfNotNull(state.user?.firstName, state.user?.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { state.user?.email ?: "" }
    val requests = state.dashboard.requests
    val directions = state.dashboard.directions
    val events = state.dashboard.events
    val todos = state.todos

    PullToRefreshBox(
        isRefreshing = state.isBusy,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // ── Compact header ──
            item {
                CompactHeader(
                    displayName = displayName,
                    requestCount = requests.size,
                    todoCount = todos.size,
                    eventCount = events.size,
                    directionCount = directions.size,
                    onRefresh = onRefresh,
                    onProfile = { onOpenSection(MainSection.PROFILE) },
                )
            }

            // ── Error ──
            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ErrorBanner(state.errorMessage)
                    }
                }
            }

            // ── Quick navigation row ──
            item {
                QuickNav(
                    requestCount = requests.size,
                    onOpenSection = onOpenSection,
                )
            }

            // ── Requests widget ──
            item {
                WidgetCard(
                    title = "Запросы",
                    count = requests.size,
                    icon = Icons.Rounded.Inbox,
                    iconBg = Violet050,
                    iconTint = Violet600,
                    emptyText = "Нет активных запросов",
                ) {
                    if (requests.isEmpty()) {
                        WidgetEmptyState("Входящих запросов пока нет")
                    } else {
                        requests.take(5).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.Inbox,
                                iconBg = Violet050,
                                iconTint = Violet600,
                                onOpenObject = onOpenObject,
                            )
                        }
                        if (requests.size > 5) {
                            WidgetDivider()
                            WidgetShowMore(
                                label = "Все запросы (${requests.size})",
                                onClick = { onOpenSection(MainSection.INBOX) },
                            )
                        }
                    }
                }
            }

            // ── Todos widget ──
            item {
                WidgetCard(
                    title = "Мои задачи",
                    count = todos.size,
                    icon = Icons.Rounded.CheckCircleOutline,
                    iconBg = Emerald050,
                    iconTint = Emerald600,
                    emptyText = "Нет задач",
                ) {
                    if (todos.isEmpty()) {
                        WidgetEmptyState("Задач пока нет — создайте первую")
                    } else {
                        todos.take(5).forEachIndexed { index, todo ->
                            if (index > 0) WidgetDivider()
                            TodoListItem(
                                title = todo.title,
                                isDone = todo.isDone,
                                dueDate = todo.dueDate,
                            )
                        }
                        if (todos.size > 5) {
                            WidgetDivider()
                            WidgetShowMore(
                                label = "Все задачи (${todos.size})",
                                onClick = { onOpenSection(MainSection.TODOS) },
                            )
                        }
                    }
                }
            }

            // ── Directions widget ──
            if (directions.isNotEmpty()) {
                item {
                    WidgetCard(
                        title = "Активные направления",
                        count = directions.size,
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        iconBg = Primary050,
                        iconTint = Primary600,
                    ) {
                        directions.take(5).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.FolderOpen,
                                iconBg = Primary050,
                                iconTint = Primary500,
                                onOpenObject = onOpenObject,
                            )
                        }
                        if (directions.size > 5) {
                            WidgetDivider()
                            WidgetShowMore(
                                label = "Все направления (${directions.size})",
                                onClick = { onOpenSection(MainSection.PROJECTS) },
                            )
                        }
                    }
                }
            }

            // ── Events widget ──
            item {
                WidgetCard(
                    title = "Лента событий",
                    count = events.size,
                    icon = Icons.Rounded.Notifications,
                    iconBg = Rose050,
                    iconTint = Rose600,
                ) {
                    if (events.isEmpty()) {
                        WidgetEmptyState("Событий пока нет")
                    } else {
                        events.take(6).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.Notifications,
                                iconBg = Rose050,
                                iconTint = Rose600,
                                onOpenObject = onOpenObject,
                            )
                        }
                    }
                }
            }

            // ── Secondary navigation ──
            item {
                SecondaryNav(onOpenSection = onOpenSection)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Compact Header
// ═══════════════════════════════════════════════════════════════

@Composable
private fun CompactHeader(
    displayName: String,
    requestCount: Int,
    todoCount: Int,
    eventCount: Int,
    directionCount: Int,
    onRefresh: () -> Unit,
    onProfile: () -> Unit,
) {
    val todayLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Locale("ru")))

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Row 1: greeting + profile/refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "$greeting, $displayName",
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray800,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    todayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Refresh, "Обновить", tint = Gray400, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onProfile, modifier = Modifier.size(40.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Primary600),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Row 2: metric pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricPill(Modifier.weight(1f), "Запросы", requestCount, Violet600, Violet050)
            MetricPill(Modifier.weight(1f), "Задачи", todoCount, Emerald600, Emerald050)
            MetricPill(Modifier.weight(1f), "События", eventCount, Rose600, Rose050)
            MetricPill(Modifier.weight(1f), "Поток", directionCount, Primary600, Primary050)
        }
    }

    HorizontalDivider(thickness = 1.dp, color = Gray200.copy(alpha = 0.8f))
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    accentColor: Color,
    bgColor: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (count > 0) bgColor else Gray050,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) accentColor else Gray400,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (count > 0) accentColor.copy(alpha = 0.7f) else Gray400,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Quick Navigation
// ═══════════════════════════════════════════════════════════════

@Composable
private fun QuickNav(
    requestCount: Int,
    onOpenSection: (MainSection) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { NavChip("Проекты", Icons.Rounded.FolderOpen) { onOpenSection(MainSection.PROJECTS) } }
        item {
            NavChip(
                label = if (requestCount > 0) "Входящие ($requestCount)" else "Входящие",
                icon = Icons.Rounded.Inbox,
                highlight = requestCount > 0,
            ) { onOpenSection(MainSection.INBOX) }
        }
        item { NavChip("Задачи", Icons.Rounded.CheckCircle) { onOpenSection(MainSection.TODOS) } }
        item { NavChip("Поиск", Icons.Rounded.Search) { onOpenSection(MainSection.SEARCH) } }
        item { NavChip("Знания", Icons.Rounded.AutoStories) { onOpenSection(MainSection.KNOWLEDGE) } }
        item { NavChip("Новости", Icons.Rounded.Newspaper) { onOpenSection(MainSection.NEWS) } }
    }
}

@Composable
private fun NavChip(
    label: String,
    icon: ImageVector,
    highlight: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (highlight) Primary600 else Color.White,
        border = if (!highlight) {
            androidx.compose.foundation.BorderStroke(1.dp, Gray200)
        } else null,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (highlight) Color.White else Gray600,
                modifier = Modifier.size(16.dp),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlight) Color.White else Gray800,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Widget Card (matches web WidgetCard component)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WidgetCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    emptyText: String = "",
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon with colored background
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconBg,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
            }
            // Count badge
            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = iconBg,
                ) {
                    Text(
                        count.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = iconTint,
                    )
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = Gray100)

        // Body
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Widget List Item (matches web widget rows)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WidgetListItem(
    item: DashboardItemDto,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onOpenObject: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onOpenObject(item.id) },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Small icon
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = iconBg,
            modifier = Modifier.size(28.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(Modifier.width(12.dp))

        // Title + meta
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Gray800,
            )
            val metaParts = mutableListOf<String>()
            item.type?.let { metaParts.add(it) }
            (item.dueDate ?: item.startDate)?.let { metaParts.add(it) }
            if (metaParts.isNotEmpty()) {
                Text(
                    metaParts.joinToString(" \u00B7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Status badge
        item.status?.takeIf { it.isNotBlank() }?.let { status ->
            StatusBadge(status)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "in_progress", "в работе" -> Primary050 to Primary600
        "completed", "завершён", "завершен" -> Emerald050 to Emerald600
        "on_hold", "на паузе" -> Amber050 to Amber600
        "cancelled", "отменён", "отменен" -> Red050 to Red600
        "not_started", "не начат" -> Gray100 to Gray500
        else -> Gray100 to Gray600
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
// Todo List Item
// ═══════════════════════════════════════════════════════════════

@Composable
private fun TodoListItem(
    title: String,
    isDone: Boolean,
    dueDate: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isDone) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircleOutline,
            contentDescription = null,
            tint = if (isDone) Emerald600 else Gray300,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = if (isDone) Gray400 else Gray800,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (dueDate != null) {
            Spacer(Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    null,
                    tint = Gray400,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    dueDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Widget helpers
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WidgetDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Gray050,
    )
}

@Composable
private fun WidgetEmptyState(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            null,
            tint = Gray300,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = Gray400,
        )
    }
}

@Composable
private fun WidgetShowMore(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = Primary600,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            null,
            tint = Primary600,
            modifier = Modifier.size(14.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Secondary Navigation
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SecondaryNav(onOpenSection: (MainSection) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(
            "ЕЩЁ",
            style = MaterialTheme.typography.labelMedium,
            color = Gray400,
            modifier = Modifier.padding(vertical = 8.dp),
            letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 2,
        )
        SecondaryNavItem("Шаблоны документов", Icons.Rounded.Description) { onOpenSection(MainSection.TEMPLATES) }
        SecondaryNavItem("Отчёты", Icons.AutoMirrored.Rounded.TrendingUp) { onOpenSection(MainSection.REPORTS) }
    }
}

@Composable
private fun SecondaryNavItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Gray500, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = Gray800,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            null,
            tint = Gray300,
            modifier = Modifier.size(14.dp),
        )
    }
}
