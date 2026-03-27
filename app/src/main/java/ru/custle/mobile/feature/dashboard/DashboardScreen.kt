@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package ru.custle.mobile.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.custle.mobile.core.model.DashboardItemDto
import ru.custle.mobile.core.ui.components.ErrorBanner
import ru.custle.mobile.core.ui.theme.Amber100
import ru.custle.mobile.core.ui.theme.Amber500
import ru.custle.mobile.core.ui.theme.Brick100
import ru.custle.mobile.core.ui.theme.Brick300
import ru.custle.mobile.core.ui.theme.Brick500
import ru.custle.mobile.core.ui.theme.Brick600
import ru.custle.mobile.core.ui.theme.Green050
import ru.custle.mobile.core.ui.theme.Green100
import ru.custle.mobile.core.ui.theme.Green700
import ru.custle.mobile.core.ui.theme.Green800
import ru.custle.mobile.core.ui.theme.Green900
import ru.custle.mobile.core.ui.theme.Info100
import ru.custle.mobile.core.ui.theme.Info500
import ru.custle.mobile.core.ui.theme.Neutral400
import ru.custle.mobile.core.ui.theme.Sand050
import ru.custle.mobile.core.ui.theme.Sand100
import ru.custle.mobile.core.ui.theme.Sand200
import ru.custle.mobile.core.ui.theme.Sand300
import ru.custle.mobile.core.ui.theme.Success100
import ru.custle.mobile.core.ui.theme.Success500
import ru.custle.mobile.navigation.CustleUiState
import ru.custle.mobile.navigation.MainSection
import java.time.LocalDate
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
    val firstName = state.user?.firstName?.takeIf { it.isNotBlank() } ?: displayName
    val requests = state.dashboard.requests
    val directions = state.dashboard.directions
    val events = state.dashboard.events
    val attentionCount = requests.size + events.size

    PullToRefreshBox(
        isRefreshing = state.isBusy,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // ── Header with greeting ──
            item {
                DashboardHeader(
                    firstName = firstName,
                    onRefresh = onRefresh,
                    onProfile = { onOpenSection(MainSection.PROFILE) },
                )
            }

            // ── Error ──
            if (!state.errorMessage.isNullOrBlank()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ErrorBanner(state.errorMessage)
                    }
                }
            }

            // ── Stats row ──
            item {
                StatsRow(
                    requests = requests.size,
                    directions = directions.size,
                    events = events.size,
                )
            }

            // ── Quick actions grid ──
            item {
                QuickActionsRow(
                    attentionCount = attentionCount,
                    onOpenSection = onOpenSection,
                )
            }

            // ── Attention items ──
            if (attentionCount > 0) {
                item {
                    SectionHeader(
                        title = "Требует внимания",
                        count = attentionCount,
                        accentColor = Brick500,
                    )
                }
                val urgentItems = (requests.take(4) + events.take(3)).take(5)
                itemsIndexed(urgentItems) { index, item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ),
                    ) {
                        AttentionCard(
                            item = item,
                            isFirst = index == 0,
                            onOpenObject = onOpenObject,
                        )
                    }
                }
            }

            // ── Active flow (directions) ──
            if (directions.isNotEmpty()) {
                item {
                    SectionHeader(title = "Рабочий поток", count = directions.size)
                }
                item {
                    FlowCarousel(items = directions, onOpenObject = onOpenObject)
                }
            }

            // ── Recent signals (events) ──
            if (events.isNotEmpty()) {
                item {
                    SectionHeader(title = "Последние события", count = events.size)
                }
                items(events.take(5)) { item ->
                    EventRow(item = item, onOpenObject = onOpenObject)
                }
            }

            // ── Empty state ──
            if (attentionCount == 0 && directions.isEmpty() && events.isEmpty() && !state.isBusy) {
                item {
                    EmptyDashboard(onOpenSection = onOpenSection)
                }
            }

            // ── Explore section ──
            item {
                ExploreSection(onOpenSection = onOpenSection)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Header
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DashboardHeader(
    firstName: String,
    onRefresh: () -> Unit,
    onProfile: () -> Unit,
) {
    val todayLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

    val greeting = when (LocalDate.now().let {
        java.time.LocalTime.now().hour
    }) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Green900, Green800, Sand050),
                    startY = 0f,
                    endY = 500f,
                ),
            )
            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 28.dp),
    ) {
        Column {
            // Top bar: date + actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    todayLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Green100.copy(alpha = 0.7f),
                )
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = "Обновить",
                            tint = Green100.copy(alpha = 0.7f),
                        )
                    }
                    IconButton(onClick = onProfile) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Green700),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Профиль",
                                tint = Green100,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Greeting
            Text(
                "$greeting,",
                style = MaterialTheme.typography.bodyLarge,
                color = Sand200,
            )
            Text(
                firstName,
                style = MaterialTheme.typography.displaySmall,
                color = Sand050,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Stats
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatsRow(
    requests: Int,
    directions: Int,
    events: Int,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(top = 20.dp),
    ) {
        item {
            StatChip(
                value = requests.toString(),
                label = "Запросов",
                icon = Icons.Rounded.Inbox,
                containerColor = if (requests > 0) Brick100 else Sand100,
                accentColor = if (requests > 0) Brick500 else Neutral400,
            )
        }
        item {
            StatChip(
                value = directions.toString(),
                label = "Направлений",
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                containerColor = if (directions > 0) Green050 else Sand100,
                accentColor = if (directions > 0) Green700 else Neutral400,
            )
        }
        item {
            StatChip(
                value = events.toString(),
                label = "Событий",
                icon = Icons.Rounded.Notifications,
                containerColor = if (events > 0) Amber100 else Sand100,
                accentColor = if (events > 0) Amber500 else Neutral400,
            )
        }
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Quick Actions
// ═══════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsRow(
    attentionCount: Int,
    onOpenSection: (MainSection) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = "Проекты",
                icon = Icons.Rounded.FolderOpen,
                containerColor = Green900,
                contentColor = Sand050,
                onClick = { onOpenSection(MainSection.PROJECTS) },
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = if (attentionCount > 0) "Входящие" else "Задачи",
                icon = if (attentionCount > 0) Icons.Rounded.Inbox else Icons.Rounded.CheckCircle,
                containerColor = if (attentionCount > 0) Brick600 else Sand100,
                contentColor = if (attentionCount > 0) Color.White else Green900,
                badge = if (attentionCount > 0) attentionCount.toString() else null,
                onClick = {
                    if (attentionCount > 0) onOpenSection(MainSection.INBOX)
                    else onOpenSection(MainSection.TODOS)
                },
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                title = "Поиск",
                icon = Icons.Rounded.Search,
                containerColor = Sand100,
                contentColor = Green900,
                onClick = { onOpenSection(MainSection.SEARCH) },
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp),
                )
                if (badge != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(0.dp),
                        shape = CircleShape,
                        color = Color.White,
                    ) {
                        Text(
                            badge,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Brick600,
                        )
                    }
                }
            }
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Section Header
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    count: Int? = null,
    accentColor: Color = Green900,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (count != null && count > 0) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.12f),
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Attention Cards
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AttentionCard(
    item: DashboardItemDto,
    isFirst: Boolean,
    onOpenObject: (String) -> Unit,
) {
    val statusLabel = item.status?.takeIf { it.isNotBlank() }
    val statusColor = when (statusLabel?.lowercase()) {
        "in_progress", "в работе" -> Info500
        "completed", "завершён" -> Success500
        "on_hold", "на паузе" -> Amber500
        else -> Brick500
    }
    val statusBg = when (statusLabel?.lowercase()) {
        "in_progress", "в работе" -> Info100
        "completed", "завершён" -> Success100
        "on_hold", "на паузе" -> Amber100
        else -> Brick100
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onOpenObject(item.id) },
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFirst) Sand050 else MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFirst) 3.dp else 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isFirst) Brick500 else Brick300),
            )

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title ?: item.name ?: item.id,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (statusLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusBg,
                        ) {
                            Text(
                                statusLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                            )
                        }
                    }
                    item.type?.let { type ->
                        Text(
                            type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val date = item.dueDate ?: item.startDate
                    if (date != null) {
                        Text(
                            date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Flow Carousel (Directions)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FlowCarousel(
    items: List<DashboardItemDto>,
    onOpenObject: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items.take(8)) { item ->
            FlowCard(item = item, onOpenObject = onOpenObject)
        }
    }
}

@Composable
private fun FlowCard(
    item: DashboardItemDto,
    onOpenObject: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onOpenObject(item.id) },
            ),
        colors = CardDefaults.cardColors(containerColor = Sand050),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Type badge at top
            item.type?.let { type ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Green100,
                ) {
                    Text(
                        type,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Green900,
                    )
                }
            }

            Text(
                text = item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            item.status?.takeIf { it.isNotBlank() }?.let { status ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Success500),
                    )
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val date = item.dueDate ?: item.startDate
            if (date != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Event Rows
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EventRow(
    item: DashboardItemDto,
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Sand100,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = Sand300.copy(alpha = 1f).let { Amber500 },
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(item.type, item.dueDate ?: item.startDate).joinToString(" \u00B7 ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp),
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Empty Dashboard
// ═══════════════════════════════════════════════════════════════

@Composable
private fun EmptyDashboard(onOpenSection: (MainSection) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = Green100,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Green700,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
        Text(
            "Всё спокойно",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "Нет срочных задач и событий.\nМожно заняться проектами.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Explore Section (secondary navigation)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ExploreSection(onOpenSection: (MainSection) -> Unit) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "Ещё",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExploreChip("Задачи", Icons.Rounded.CheckCircle) { onOpenSection(MainSection.TODOS) }
            ExploreChip("Знания", Icons.Rounded.AutoStories) { onOpenSection(MainSection.KNOWLEDGE) }
            ExploreChip("Новости", Icons.Rounded.Newspaper) { onOpenSection(MainSection.NEWS) }
            ExploreChip("Шаблоны", Icons.Rounded.Description) { onOpenSection(MainSection.TEMPLATES) }
        }
    }
}

@Composable
private fun ExploreChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
