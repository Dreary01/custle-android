@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.NewsDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun NewsRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { NewsViewModelFactory(container) }
    val viewModel: NewsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    NewsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpen = viewModel::open,
        onClose = viewModel::close,
    )
}

@Composable
fun NewsScreen(
    state: NewsUiState,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
    onClose: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            NewsHero(
                newsCount = state.items.size,
                hasOpenArticle = state.selectedNews != null,
                onRefresh = onRefresh,
                onClose = onClose,
            )
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        state.selectedNews?.let { news ->
            item {
                NewsDetailCard(news = news, onClose = onClose)
            }
        }
        if (state.isLoading) {
            item {
                AppSectionCard(
                    title = "Обновляем ленту",
                    hint = "Подтягиваем свежие материалы workspace.",
                ) {
                    Text(
                        "Список может немного перестроиться после загрузки.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyNewsState()
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                NewsRow(item = item, isSelected = state.selectedNews?.id == item.id, onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun NewsHero(
    newsCount: Int,
    hasOpenArticle: Boolean,
    onRefresh: () -> Unit,
    onClose: () -> Unit,
) {
    AppHeroCard(
        title = "Новости",
        subtitle = "Лента workspace в read-first режиме. Здесь важнее быстро схватить суть новости, чем показывать много служебного шума.",
        chips = buildList {
            add("$newsCount материалов" to Icons.Outlined.Newspaper)
            if (hasOpenArticle) add("Открыта детальная новость" to Icons.Outlined.Public)
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Text("Обновить")
            }
            if (hasOpenArticle) {
                OutlinedButton(onClick = onClose) {
                    Text("Свернуть материал")
                }
            }
        }
    }
}

@Composable
private fun NewsDetailCard(
    news: NewsDto,
    onClose: () -> Unit,
) {
    AppSectionCard(
        title = news.title,
        hint = listOfNotNull(news.authorName.takeIf { it.isNotBlank() }, news.createdAt.takeIf { it.isNotBlank() })
            .joinToString(" • ")
            .ifBlank { null },
    ) {
        if (news.body.isNotBlank()) {
            Text(news.body, style = MaterialTheme.typography.bodyMedium)
        }
        OutlinedButton(onClick = onClose) {
            Text("Свернуть")
        }
    }
}

@Composable
private fun NewsRow(
    item: NewsDto,
    isSelected: Boolean,
    onOpen: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(item.id) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                item.authorName.takeIf { it.isNotBlank() },
                item.createdAt.takeIf { it.isNotBlank() },
                if (item.isPublished) "Опубликовано" else "Черновик",
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.body.isNotBlank()) {
                Text(
                    text = item.body.take(180) + if (item.body.length > 180) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        "Открыто выше",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNewsState() {
    EmptyStateCard(
        title = "Новостей пока нет",
        message = "Когда лента появится, она должна ощущаться как компактная редакционная витрина, а не как технический список записей.",
    )
}

@Composable
private fun NewsChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}
