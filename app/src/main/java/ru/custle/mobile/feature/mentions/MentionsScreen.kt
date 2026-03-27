package ru.custle.mobile.feature.mentions

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.MentionDto

private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)
private val AmberDark = Color(0xFF3D2E0A)
private val AmberText = Color(0xFFE8C060)
private val RoseDark = Color(0xFF3D1A2A)
private val RoseText = Color(0xFFE88DA0)

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
    val unresolvedCount = state.items.count { !it.isResolved }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Входящие",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (unresolvedCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Text(
                                "$unresolvedCount",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
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
                    onClose = onCloseDiscussion,
                )
            }
        }

        if (state.items.isEmpty() && !state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Нет запросов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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

        item { Spacer(Modifier.height(12.dp)) }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedDiscussion)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            1.dp,
            if (isSelectedDiscussion)
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                item.discussionTitle.ifBlank { "Запрос" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            val meta = listOfNotNull(
                item.objectName,
                item.authorName.takeIf { it.isNotBlank() },
                item.createdAt.takeIf { it.isNotBlank() },
            ).joinToString(" \u00b7 ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (item.messagePreview.isNotBlank()) {
                Text(
                    item.messagePreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.objectId?.takeIf { it.isNotBlank() }?.let { objectId ->
                    TextButton(onClick = { onOpenObject(objectId) }, modifier = Modifier.height(32.dp)) {
                        Icon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Объект", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (!item.objectId.isNullOrBlank()) {
                    TextButton(
                        onClick = onOpenDiscussion,
                        enabled = !isLoadingDiscussion,
                        modifier = Modifier.height(32.dp),
                    ) {
                        if (isLoadingDiscussion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp,
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("Тред", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = onResolve,
                    enabled = !isResolving,
                    modifier = Modifier.height(32.dp),
                ) {
                    if (isResolving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = EmeraldText,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = EmeraldText,
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Решено",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldText,
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscussionPreview(
    title: String,
    messages: List<ru.custle.mobile.core.model.DiscussionMessageDto>,
    onOpenObject: (() -> Unit)?,
    onClose: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AmberDark,
        ),
        border = BorderStroke(1.dp, AmberText.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title.ifBlank { "Обсуждение" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AmberText,
                )
                Row {
                    onOpenObject?.let {
                        TextButton(onClick = it, modifier = Modifier.height(32.dp)) {
                            Icon(
                                Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AmberText.copy(alpha = 0.7f),
                            )
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = AmberText.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            if (messages.isEmpty()) {
                Text(
                    "Нет сообщений",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmberText.copy(alpha = 0.6f),
                )
            } else {
                messages.take(3).forEach { msg ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val msgMeta = listOfNotNull(
                            msg.authorName.takeIf { it.isNotBlank() },
                            msg.createdAt.takeIf { it.isNotBlank() },
                        ).joinToString(" \u00b7 ")
                        if (msgMeta.isNotBlank()) {
                            Text(
                                msgMeta,
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberText.copy(alpha = 0.7f),
                            )
                        }
                        Text(
                            msg.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberText.copy(alpha = 0.9f),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
