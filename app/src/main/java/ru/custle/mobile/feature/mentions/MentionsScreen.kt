@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.mentions

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
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.MentionDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun MentionsRoute(
    onOpenObject: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val factory = remember(container) { MentionsViewModelFactory(container) }
    val viewModel: MentionsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    MentionsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onResolve = viewModel::resolve,
        onOpenDiscussion = viewModel::openDiscussion,
        onCloseDiscussion = viewModel::closeDiscussion,
        onOpenObject = onOpenObject,
    )
}

@Composable
fun MentionsScreen(
    state: MentionsUiState,
    onRefresh: () -> Unit,
    onResolve: (String) -> Unit,
    onOpenDiscussion: (MentionDto) -> Unit,
    onCloseDiscussion: () -> Unit,
    onOpenObject: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            InboxHero(
                mentionsCount = state.items.size,
                hasDiscussionPreview = state.selectedDiscussion != null,
                onRefresh = onRefresh,
                onCloseDiscussion = onCloseDiscussion,
            )
        }
        if (state.isLoading) {
            item {
                AppSectionCard(
                    title = "Обновляем входящие",
                    hint = "Подтягиваем mentions, requests и preview выбранного треда.",
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        val selectedMention = state.items.firstOrNull { it.id == state.selectedMentionId }
        state.selectedDiscussion?.let { discussion ->
            item {
                val openObjectAction: (() -> Unit)? = selectedMention?.objectId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { objectId -> { onOpenObject(objectId) } }
                DiscussionPreview(
                    title = discussion.title,
                    messages = state.discussionMessages,
                    onOpenObject = openObjectAction,
                )
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyInboxState()
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                MentionRow(
                    item = item,
                    isResolving = state.resolvingIds.contains(item.id),
                    isLoadingDiscussion = state.loadingDiscussionId == item.id,
                    isSelectedDiscussion = state.selectedMentionId == item.id,
                    onResolve = { onResolve(item.id) },
                    onOpenObject = onOpenObject,
                    onOpenDiscussion = { onOpenDiscussion(item) },
                )
            }
        }
    }
}

@Composable
private fun InboxHero(
    mentionsCount: Int,
    hasDiscussionPreview: Boolean,
    onRefresh: () -> Unit,
    onCloseDiscussion: () -> Unit,
) {
    AppHeroCard(
        title = "Входящие",
        subtitle = "Место, где видно, что требует реакции сейчас: mentions, requests и быстрый просмотр треда без лишних переходов.",
        chips = buildList {
            add("$mentionsCount запросов" to Icons.Outlined.Notifications)
            if (hasDiscussionPreview) add("Открыт preview треда" to Icons.Outlined.Forum)
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
            if (hasDiscussionPreview) {
                OutlinedButton(onClick = onCloseDiscussion) {
                    Text("Свернуть preview")
                }
            }
        }
    }
}

@Composable
private fun MentionRow(
    item: MentionDto,
    isResolving: Boolean,
    isLoadingDiscussion: Boolean,
    isSelectedDiscussion: Boolean,
    onResolve: () -> Unit,
    onOpenObject: (String) -> Unit,
    onOpenDiscussion: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                item.discussionTitle.ifBlank { "Запрос" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(
                item.objectName,
                item.authorName.takeIf { it.isNotBlank() },
                item.createdAt.takeIf { it.isNotBlank() },
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (item.messagePreview.isNotBlank()) {
                Text(item.messagePreview, style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item.objectId?.let { objectId ->
                    OutlinedButton(onClick = { onOpenObject(objectId) }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null)
                        Text("Объект")
                    }
                }
                if (!item.objectId.isNullOrBlank()) {
                    OutlinedButton(onClick = onOpenDiscussion, enabled = !isLoadingDiscussion) {
                        Icon(Icons.Outlined.Forum, contentDescription = null)
                        Text(if (isLoadingDiscussion) "Открываем..." else "Тред")
                    }
                }
                Button(onClick = onResolve, enabled = !isResolving) {
                    Icon(Icons.Outlined.DoneAll, contentDescription = null)
                    Text(if (isResolving) "Закрываем..." else "Решено")
                }
            }
            if (isSelectedDiscussion) {
                Text(
                    "Preview обсуждения открыт выше",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DiscussionPreview(
    title: String,
    messages: List<ru.custle.mobile.core.model.DiscussionMessageDto>,
    onOpenObject: (() -> Unit)?,
) {
    AppSectionCard(
        title = title,
        hint = "Прямой просмотр треда из входящих без лишнего перехода в объект.",
    ) {
        onOpenObject?.let {
            OutlinedButton(onClick = it) {
                Text("Открыть объект")
            }
        }
        if (messages.isEmpty()) {
            Text(
                "Сообщений пока нет",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            messages.take(3).forEach { msg ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            listOfNotNull(msg.authorName.takeIf { it.isNotBlank() }, msg.createdAt.takeIf { it.isNotBlank() })
                                .joinToString(" • "),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(msg.content, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInboxState() {
    EmptyStateCard(
        title = "Новых запросов нет",
        message = "Когда кто-то упомянет тебя или пришлёт request, он появится здесь как явный actionable элемент.",
    )
}

@Composable
private fun InboxChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}
