@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.DashboardItemDto
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner
import ru.custle.mobile.core.ui.theme.Brick300
import ru.custle.mobile.core.ui.theme.Brick600
import ru.custle.mobile.core.ui.theme.Green900
import ru.custle.mobile.core.ui.theme.Sand050
import ru.custle.mobile.core.ui.theme.Sand100
import ru.custle.mobile.core.ui.theme.Sand200
import ru.custle.mobile.core.ui.theme.Sand700
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
        .ifBlank { state.user?.email ?: "Пользователь" }
    val requests = state.dashboard.requests
    val directions = state.dashboard.directions
    val events = state.dashboard.events
    val attentionCount = requests.size + events.size

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HomeHero(
                displayName = displayName,
                workspacesCount = state.workspaces.size,
                attentionCount = attentionCount,
                requestCount = requests.size,
                directionsCount = directions.size,
                eventsCount = events.size,
                onRefresh = onRefresh,
                onLogout = onLogout,
                errorMessage = state.errorMessage,
                isBusy = state.isBusy,
                onOpenSection = onOpenSection,
            )
        }
        item {
            PrimaryActionsSection(onOpenSection = onOpenSection)
        }
        item { FocusSection(requests = requests.size, directions = directions.size, events = events.size) }
        item {
            AttentionSection(
                items = requests.take(4) + events.take(2),
                onOpenObject = onOpenObject,
            )
        }
        item {
            DashboardCollectionSection(
                title = "Рабочий поток",
                hint = "Направления, которые показывают движение по живым объектам.",
                items = directions,
                emptyTitle = "Направления пока не появились",
                emptyMessage = "Когда в workspace будет больше движения, здесь появятся основные ветки работы.",
                onOpenObject = onOpenObject,
            )
        }
        item {
            SecondaryActionsSection(onOpenSection = onOpenSection)
        }
        item {
            DashboardCollectionSection(
                title = "Последние сигналы",
                hint = "События и изменения, к которым можно вернуться позже, не перегружая первый экран.",
                items = events,
                emptyTitle = "Событий пока нет",
                emptyMessage = "Блок оживает, когда в системе появляются новые изменения и сроки.",
                onOpenObject = onOpenObject,
            )
        }
    }
}

@Composable
private fun HomeContextStrip(
    workspaceCount: Int,
    requestCount: Int,
    eventsCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ContextPill(
            modifier = Modifier.weight(1f),
            label = "Среда",
            value = workspaceCount.toString(),
        )
        ContextPill(
            modifier = Modifier.weight(1f),
            label = "Запросы",
            value = requestCount.toString(),
        )
        ContextPill(
            modifier = Modifier.weight(1f),
            label = "События",
            value = eventsCount.toString(),
        )
    }
}

@Composable
private fun ContextPill(
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
                    brush = Brush.horizontalGradient(
                        listOf(
                            Sand100,
                            Sand050,
                        ),
                    ),
                    shape = RoundedCornerShape(999.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeHero(
    displayName: String,
    workspacesCount: Int,
    attentionCount: Int,
    requestCount: Int,
    directionsCount: Int,
    eventsCount: Int,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    errorMessage: String?,
    isBusy: Boolean,
    onOpenSection: (MainSection) -> Unit,
) {
    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Sand050,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HomeContextStrip(
                workspaceCount = workspacesCount,
                requestCount = requestCount,
                eventsCount = eventsCount,
            )
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Sand200.copy(alpha = 0.22f)),
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Green900,
                                    Green900.copy(alpha = 0.94f),
                                    Sand700,
                                ),
                            ),
                            shape = RoundedCornerShape(22.dp),
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Sand050.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Sand200.copy(alpha = 0.18f)),
                        ) {
                            Text(
                                "EXECUTIVE HOME",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = Sand100,
                            )
                        }
                        Text(
                            todayLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() },
                            style = MaterialTheme.typography.bodySmall,
                            color = Sand200,
                        )
                    }
                    Text(
                        "Доброе рабочее утро,\n$displayName",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Sand050,
                    )
                    Text(
                        if (attentionCount > 0) {
                            "Есть живые сигналы. Начни с входящих и объектов, а уже потом уходи в остальной контекст рабочего дня."
                        } else {
                            "Контур выглядит спокойно. Можно стартовать с проектов, задач или поиска без ощущения срочности."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Sand200,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = attentionCount.toString(),
                    label = "Требует внимания",
                    accent = Brick300.copy(alpha = 0.55f),
                )
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = directionsCount.toString(),
                    label = "Активных направлений",
                    accent = Green900.copy(alpha = 0.12f),
                )
                HeroMetricCard(
                    modifier = Modifier.weight(1f),
                    value = workspacesCount.toString(),
                    label = "Среда",
                    accent = Sand200,
                )
            }
            if (!errorMessage.isNullOrBlank()) {
                ErrorBanner(errorMessage)
            }
            if (isBusy) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            "Обновляем домашний контур и собираем свежие сигналы.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        if (attentionCount > 0) onOpenSection(MainSection.INBOX) else onOpenSection(MainSection.PROJECTS)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        if (attentionCount > 0) Icons.Outlined.Notifications else Icons.Outlined.Source,
                        contentDescription = null,
                    )
                    Text(if (attentionCount > 0) "Разобрать входящие" else "Открыть проекты")
                }
                OutlinedButton(
                    onClick = {
                        if (attentionCount > 0) onOpenSection(MainSection.PROJECTS) else onOpenSection(MainSection.TODOS)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        if (attentionCount > 0) Icons.Outlined.Source else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                    )
                    Text(if (attentionCount > 0) "Перейти к объектам" else "Перейти к задачам")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onRefresh, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Text("Освежить")
                }
                OutlinedButton(onClick = onLogout, modifier = Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Text("Выйти")
                }
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
                textAlign = TextAlign.Center,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FocusSection(
    requests: Int,
    directions: Int,
    events: Int,
) {
    HomePanel(
        title = "Фокус дня",
        hint = "Короткий аналитический срез, который поддерживает главный входной контур, а не спорит с ним.",
        accent = Green900.copy(alpha = 0.10f),
        marker = "DAILY FOCUS",
    ) {
        Text(
            text = if (requests + events > 0) {
                "День живой: смотри на внимание и запросы, а затем переходи к потоку."
            } else {
                "День спокойнее обычного: поток важнее срочных сигналов."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FocusStatCard(
                modifier = Modifier.weight(1f),
                label = "Внимание",
                value = requests + events,
                tone = if (requests + events > 0) "Нужно действие" else "Спокойно",
                accent = Brick300.copy(alpha = 0.65f),
            )
            FocusStatCard(
                modifier = Modifier.weight(1f),
                label = "Запросы",
                value = requests,
                tone = if (requests > 0) "Пора ответить" else "Чисто",
                accent = Sand200,
            )
            FocusStatCard(
                modifier = Modifier.weight(1f),
                label = "Поток",
                value = directions,
                tone = if (directions > 0) "Есть движение" else "Пока тихо",
                accent = Green900.copy(alpha = 0.18f),
            )
        }
    }
}

@Composable
private fun FocusStatCard(
    label: String,
    value: Int,
    tone: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = accent,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                tone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrimaryActionsSection(
    onOpenSection: (MainSection) -> Unit,
) {
    HomePanel(
        title = "Основные входы",
        hint = "Сразу под главным контекстом дня: сначала основные траектории, потом вторичный контур.",
        accent = Green900.copy(alpha = 0.10f),
        marker = "PRIMARY ROUTES",
    ) {
        FeaturedActionCard(
            title = "Проекты",
            subtitle = "Основной вход в дерево, карточки объектов и рабочий поток по живым сущностям.",
            meta = "Главный контур",
            icon = Icons.Outlined.Source,
            accent = Green900,
            container = Green900,
            contentColor = Sand050,
            onClick = { onOpenSection(MainSection.PROJECTS) },
        )
        FeaturedActionCard(
            title = "Входящие",
            subtitle = "Быстрый разбор mentions, requests и контекста, который просит реакции сейчас.",
            meta = "Если день горячий",
            icon = Icons.Outlined.Notifications,
            accent = Brick600,
            container = Brick300.copy(alpha = 0.45f),
            contentColor = Green900,
            onClick = { onOpenSection(MainSection.INBOX) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeActionCard(
                modifier = Modifier.weight(1f),
                title = "Задачи",
                subtitle = "Короткий операционный список",
                icon = Icons.Outlined.CheckCircle,
                accent = Sand700,
                container = Sand100,
                onClick = { onOpenSection(MainSection.TODOS) },
            )
            HomeActionCard(
                modifier = Modifier.weight(1f),
                title = "Поиск",
                subtitle = "Найти объект, документ или тред",
                icon = Icons.Outlined.Search,
                accent = Green900,
                container = Green900.copy(alpha = 0.08f),
                onClick = { onOpenSection(MainSection.SEARCH) },
            )
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = container,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accent.copy(alpha = 0.16f),
            ) {
                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accent)
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Открыть",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
        }
    }
}

@Composable
private fun FeaturedActionCard(
    title: String,
    subtitle: String,
    meta: String,
    icon: ImageVector,
    accent: Color,
    container: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = accent.copy(alpha = if (contentColor == Sand050) 0.24f else 0.16f),
            ) {
                Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = contentColor)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(meta, style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = 0.82f))
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.86f))
            }
            Text(
                "Открыть",
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun AttentionSection(
    items: List<DashboardItemDto>,
    onOpenObject: (String) -> Unit,
) {
    HomePanel(
        title = "Нужно внимание",
        hint = "Сюда попадает то, что выглядит наиболее живым и требующим реакции прямо сейчас.",
        accent = Brick300.copy(alpha = 0.28f),
        marker = "ATTENTION",
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Sand050,
            border = BorderStroke(1.dp, Brick300.copy(alpha = 0.45f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Сигналы на сейчас", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (items.isEmpty()) "Срочных элементов нет." else "Сначала открой верхние карточки, потом иди в поток.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (items.isEmpty()) "0" else "${items.size} сигн.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Brick600,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (items.isEmpty()) {
            EmptyStateCard(
                title = "Срочных сигналов нет",
                message = "Это хороший сценарий: можно идти в проекты или задачи без чувства, что что-то уже горит.",
            )
        } else {
            items.forEach { item ->
                AttentionCard(item = item, onOpenObject = onOpenObject)
            }
        }
    }
}

@Composable
private fun AttentionCard(
    item: DashboardItemDto,
    onOpenObject: (String) -> Unit,
) {
    val statusLabel = item.status?.takeIf { it.isNotBlank() } ?: "Нужен просмотр"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenObject(item.id) },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Brick300.copy(alpha = 0.7f)),
        color = Sand050,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Brick300.copy(alpha = 0.42f),
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Brick600,
                    )
                }
                Text(
                    "Открыть",
                    style = MaterialTheme.typography.labelLarge,
                    color = Green900,
                )
            }
            Text(
                text = item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(item.type, item.dueDate ?: item.startDate).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SecondaryActionsSection(
    onOpenSection: (MainSection) -> Unit,
) {
    HomePanel(
        title = "Дальше по работе",
        hint = "Менее срочные, но частые переходы, которые всё ещё должны быть рядом с главным контуром.",
        accent = Sand100,
        marker = "SUPPORT",
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecondaryActionChip("Знания", Icons.Outlined.AutoStories) { onOpenSection(MainSection.KNOWLEDGE) }
            SecondaryActionChip("Новости", Icons.Outlined.Newspaper) { onOpenSection(MainSection.NEWS) }
            SecondaryActionChip("Шаблоны", Icons.Outlined.Description) { onOpenSection(MainSection.TEMPLATES) }
            SecondaryActionChip("Профиль", Icons.Outlined.TaskAlt) { onOpenSection(MainSection.PROFILE) }
        }
    }
}

@Composable
private fun SecondaryActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(label, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun DashboardCollectionSection(
    title: String,
    hint: String,
    items: List<DashboardItemDto>,
    emptyTitle: String,
    emptyMessage: String,
    onOpenObject: (String) -> Unit,
) {
    HomePanel(
        title = title,
        hint = hint,
        accent = Green900.copy(alpha = 0.06f),
        marker = if (title == "Рабочий поток") "FLOW" else "SIGNALS",
    ) {
        if (items.isEmpty()) {
            EmptyStateCard(
                title = emptyTitle,
                message = emptyMessage,
            )
        } else {
            items.take(5).forEach { item ->
                DashboardListRow(item = item, onOpenObject = onOpenObject)
            }
        }
    }
}

@Composable
private fun DashboardListRow(
    item: DashboardItemDto,
    onOpenObject: (String) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenObject(item.id) },
        shape = RoundedCornerShape(18.dp),
        color = Sand100,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                item.title ?: item.name ?: item.id,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(item.type, item.status, item.startDate, item.dueDate).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )
            Text(
                "Открыть объект",
                style = MaterialTheme.typography.labelLarge,
                color = Green900,
            )
        }
    }
}

@Composable
private fun HomePanel(
    title: String,
    hint: String,
    accent: Color,
    marker: String,
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
                            accent.copy(alpha = 0.9f),
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
