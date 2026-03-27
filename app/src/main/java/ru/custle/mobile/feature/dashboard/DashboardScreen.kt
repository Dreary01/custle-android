@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package ru.custle.mobile.feature.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inbox
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
import ru.custle.mobile.core.ui.theme.Amber600
import ru.custle.mobile.core.ui.theme.Emerald600
import ru.custle.mobile.core.ui.theme.Primary400
import ru.custle.mobile.core.ui.theme.Primary500
import ru.custle.mobile.core.ui.theme.Red600
import ru.custle.mobile.core.ui.theme.Rose600
import ru.custle.mobile.core.ui.theme.Violet600
import ru.custle.mobile.navigation.CustleUiState
import ru.custle.mobile.navigation.MainSection
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Dark-friendly accent tones (muted for dark backgrounds) ──
private val VioletDark = Color(0xFF3D2A6E)
private val VioletText = Color(0xFFB4A0E8)
private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)
private val RoseDark = Color(0xFF3D1A2A)
private val RoseText = Color(0xFFE88DA0)
private val BlueDark = Color(0xFF1A2A4D)
private val BlueText = Color(0xFF8DB0F0)
private val AmberDark = Color(0xFF3D2E0A)
private val AmberText = Color(0xFFE8C060)
private val RedDark = Color(0xFF3D1515)
private val RedText = Color(0xFFE88080)

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

            // ── Quick navigation ──
            item {
                QuickNav(requestCount = requests.size, onOpenSection = onOpenSection)
            }

            // ── Requests widget ──
            item {
                WidgetCard(
                    title = "Запросы",
                    count = requests.size,
                    icon = Icons.Rounded.Inbox,
                    iconBg = VioletDark,
                    iconTint = VioletText,
                ) {
                    if (requests.isEmpty()) {
                        WidgetEmptyState("Входящих запросов пока нет")
                    } else {
                        requests.take(5).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.Inbox,
                                iconBg = VioletDark,
                                iconTint = VioletText,
                                onOpenObject = onOpenObject,
                            )
                        }
                        if (requests.size > 5) {
                            WidgetDivider()
                            WidgetShowMore("Все запросы (${requests.size})") { onOpenSection(MainSection.INBOX) }
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
                    iconBg = EmeraldDark,
                    iconTint = EmeraldText,
                ) {
                    if (todos.isEmpty()) {
                        WidgetEmptyState("Задач пока нет — создайте первую")
                    } else {
                        todos.take(5).forEachIndexed { index, todo ->
                            if (index > 0) WidgetDivider()
                            TodoListItem(title = todo.title, isDone = todo.isDone, dueDate = todo.dueDate)
                        }
                        if (todos.size > 5) {
                            WidgetDivider()
                            WidgetShowMore("Все задачи (${todos.size})") { onOpenSection(MainSection.TODOS) }
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
                        iconBg = BlueDark,
                        iconTint = BlueText,
                    ) {
                        directions.take(5).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.FolderOpen,
                                iconBg = BlueDark,
                                iconTint = BlueText,
                                onOpenObject = onOpenObject,
                            )
                        }
                        if (directions.size > 5) {
                            WidgetDivider()
                            WidgetShowMore("Все направления (${directions.size})") { onOpenSection(MainSection.PROJECTS) }
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
                    iconBg = RoseDark,
                    iconTint = RoseText,
                ) {
                    if (events.isEmpty()) {
                        WidgetEmptyState("Событий пока нет")
                    } else {
                        events.take(6).forEachIndexed { index, item ->
                            if (index > 0) WidgetDivider()
                            WidgetListItem(
                                item = item,
                                icon = Icons.Rounded.Notifications,
                                iconBg = RoseDark,
                                iconTint = RoseText,
                                onOpenObject = onOpenObject,
                            )
                        }
                    }
                }
            }

            // ── Secondary navigation ──
            item { SecondaryNav(onOpenSection = onOpenSection) }
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$greeting, $displayName",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    todayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Rounded.Refresh, "Обновить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onProfile, modifier = Modifier.size(40.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Person, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Metric pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricPill(Modifier.weight(1f), "Запросы", requestCount, VioletText, VioletDark)
            MetricPill(Modifier.weight(1f), "Задачи", todoCount, EmeraldText, EmeraldDark)
            MetricPill(Modifier.weight(1f), "События", eventCount, RoseText, RoseDark)
            MetricPill(Modifier.weight(1f), "Поток", directionCount, BlueText, BlueDark)
        }
    }

    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun MetricPill(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    accentColor: Color,
    bgColor: Color,
) {
    val inactive = MaterialTheme.colorScheme.surfaceVariant
    val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (count > 0) bgColor else inactive,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) accentColor else inactiveText,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (count > 0) accentColor.copy(alpha = 0.7f) else inactiveText,
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
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        border = if (!highlight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon, null,
                tint = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlight) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Widget Card
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WidgetCard(
    title: String,
    count: Int,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
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

        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Widget List Item
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val metaParts = mutableListOf<String>()
            item.type?.let { metaParts.add(it) }
            (item.dueDate ?: item.startDate)?.let { metaParts.add(it) }
            if (metaParts.isNotEmpty()) {
                Text(
                    metaParts.joinToString(" \u00B7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        item.status?.takeIf { it.isNotBlank() }?.let { status ->
            StatusBadge(status)
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "in_progress", "в работе" -> BlueDark to BlueText
        "completed", "завершён", "завершен" -> EmeraldDark to EmeraldText
        "on_hold", "на паузе" -> AmberDark to AmberText
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
            tint = if (isDone) EmeraldText else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
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
                    Icons.Rounded.CalendarToday, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    dueDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
            Icons.Rounded.CheckCircle, null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward, null,
            tint = MaterialTheme.colorScheme.primary,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward, null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(14.dp),
        )
    }
}
