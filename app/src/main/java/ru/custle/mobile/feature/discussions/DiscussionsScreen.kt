@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.discussions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.MarkChatUnread
import androidx.compose.material.icons.outlined.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.DiscussionDto
import ru.custle.mobile.core.model.DiscussionMessageDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun DiscussionsRoute(
    objectId: String,
    objectName: String,
    initialDiscussionId: String? = null,
    onInitialDiscussionConsumed: () -> Unit = {},
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val factory = remember(container) { DiscussionsViewModelFactory(container) }
    val viewModel: DiscussionsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(objectId) {
        viewModel.loadObject(objectId = objectId, objectName = objectName)
    }

    LaunchedEffect(initialDiscussionId) {
        if (!initialDiscussionId.isNullOrBlank()) {
            viewModel.openDiscussion(initialDiscussionId)
            onInitialDiscussionConsumed()
        }
    }

    DiscussionsScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onOpenDiscussion = viewModel::openDiscussion,
        onCloseDiscussion = viewModel::closeDiscussion,
        onCreateDiscussion = viewModel::createDiscussion,
        onRenameDiscussion = viewModel::renameDiscussion,
        onSetDiscussionClosed = viewModel::setDiscussionClosed,
        onDeleteDiscussion = viewModel::deleteDiscussion,
        onSelectMentionUser = viewModel::selectMentionUser,
        onSetSendAsRequest = viewModel::setSendAsRequest,
        onSendMessage = viewModel::sendMessage,
        onCreateNestedDiscussion = viewModel::createNestedDiscussion,
        onUpdateMessage = viewModel::updateMessage,
        onDeleteMessage = viewModel::deleteMessage,
    )
}

@Composable
fun DiscussionsScreen(
    state: DiscussionsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDiscussion: (String) -> Unit,
    onCloseDiscussion: () -> Unit,
    onCreateDiscussion: (String) -> Unit,
    onRenameDiscussion: (String) -> Unit,
    onSetDiscussionClosed: (Boolean) -> Unit,
    onDeleteDiscussion: () -> Unit,
    onSelectMentionUser: (String?) -> Unit,
    onSetSendAsRequest: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    onCreateNestedDiscussion: (String, String) -> Unit,
    onUpdateMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
) {
    var newTitle by rememberSaveable(state.objectId) { mutableStateOf("") }
    var reply by rememberSaveable(state.selectedDiscussion?.id) { mutableStateOf("") }
    var discussionDraftTitle by rememberSaveable(state.selectedDiscussion?.id) {
        mutableStateOf(state.selectedDiscussion?.title.orEmpty())
    }
    var confirmDeleteDiscussion by rememberSaveable(state.selectedDiscussion?.id) { mutableStateOf(false) }
    var confirmDeleteMessage by remember { mutableStateOf<DiscussionMessageDto?>(null) }

    LaunchedEffect(state.selectedDiscussion?.id, state.selectedDiscussion?.title) {
        discussionDraftTitle = state.selectedDiscussion?.title.orEmpty()
    }

    if (confirmDeleteDiscussion && state.selectedDiscussion != null) {
        DestructiveConfirmDialog(
            title = "Удалить тему?",
            message = "Тема \"${state.selectedDiscussion.title}\" исчезнет вместе с текущим контекстом разговора.",
            isBusy = state.mutatingDiscussionId == state.selectedDiscussion.id,
            onConfirm = {
                onDeleteDiscussion()
                confirmDeleteDiscussion = false
            },
            onDismiss = { confirmDeleteDiscussion = false },
        )
    }

    confirmDeleteMessage?.let { message ->
        DestructiveConfirmDialog(
            title = "Удалить сообщение?",
            message = "Сообщение \"${message.content.take(120)}\" будет удалено из обсуждения.",
            isBusy = state.mutatingMessageId == message.id,
            onConfirm = {
                onDeleteMessage(message.id)
                confirmDeleteMessage = null
            },
            onDismiss = { confirmDeleteMessage = null },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DiscussionsHero(
                objectName = state.objectName,
                selectedDiscussion = state.selectedDiscussion,
                discussionsCount = state.discussions.size,
                onBack = if (state.selectedDiscussion == null) onBack else onCloseDiscussion,
                onRefresh = onRefresh,
            )
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        if (state.selectedDiscussion == null) {
            item {
                NewDiscussionCard(
                    title = newTitle,
                    onTitleChange = { newTitle = it },
                    onCreate = {
                        onCreateDiscussion(newTitle.trim())
                        newTitle = ""
                    },
                )
            }
            if (state.discussions.isEmpty() && !state.isLoading) {
                item {
                    EmptyStateCard(
                        title = "Обсуждений пока нет",
                        message = "Создай первую тему, чтобы начать рабочий разговор по объекту.",
                    )
                }
            } else {
                items(state.discussions, key = { it.id }) { item ->
                    DiscussionRow(item = item, onOpen = onOpenDiscussion)
                }
            }
        } else {
            item {
                DiscussionHeaderCard(
                    discussion = state.selectedDiscussion,
                    draftTitle = discussionDraftTitle,
                    isMutating = state.mutatingDiscussionId == state.selectedDiscussion.id,
                    onTitleChange = { discussionDraftTitle = it },
                    onRename = { onRenameDiscussion(discussionDraftTitle.trim()) },
                    onToggleClosed = { onSetDiscussionClosed(!state.selectedDiscussion.isClosed) },
                    onDelete = { confirmDeleteDiscussion = true },
                )
            }
            if (state.nestedDiscussions.isNotEmpty()) {
                item {
                    SectionBlock(title = "Вложенные темы", hint = "Подразговоры внутри текущей дискуссии") {
                        state.nestedDiscussions.forEach { nested ->
                            DiscussionRow(item = nested, onOpen = onOpenDiscussion)
                        }
                    }
                }
            }
            if (state.messages.isEmpty() && !state.isLoadingMessages) {
                item {
                    EmptyStateCard(
                        title = "Сообщений пока нет",
                        message = "Начни обсуждение первым сообщением ниже.",
                    )
                }
            } else {
                items(state.messages, key = { it.id }) { item ->
                    MessageRow(
                        item = item,
                        canEdit = state.currentUserId != null && item.createdBy == state.currentUserId,
                        isMutating = state.mutatingMessageId == item.id,
                        onOpenNestedDiscussion = onOpenDiscussion,
                        onCreateNestedDiscussion = { onCreateNestedDiscussion(item.id, it) },
                        onUpdate = { onUpdateMessage(item.id, it) },
                        onDelete = { confirmDeleteMessage = item },
                    )
                }
            }
            item {
                ReplyComposer(
                    reply = reply,
                    workspaceMembers = state.workspaceMembers,
                    selectedMentionUserId = state.selectedMentionUserId,
                    sendAsRequest = state.sendAsRequest,
                    isClosed = state.selectedDiscussion.isClosed,
                    onReplyChange = { reply = it },
                    onSelectMentionUser = onSelectMentionUser,
                    onSetSendAsRequest = onSetSendAsRequest,
                    onSend = {
                        onSendMessage(reply.trim())
                        reply = ""
                    },
                )
            }
        }
    }
}

@Composable
private fun DiscussionsHero(
    objectName: String,
    selectedDiscussion: DiscussionDto?,
    discussionsCount: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    AppHeroCard(
        title = if (selectedDiscussion == null) objectName.ifBlank { "Обсуждения" } else selectedDiscussion.title,
        subtitle = if (selectedDiscussion == null) {
            "Темы и рабочие разговоры по объекту. Здесь важно быстро понять, где активность и куда входить."
        } else {
            "Внутри темы доступны ответы, mentions, requests и вложенные обсуждения."
        },
        chips = buildList {
            add("$discussionsCount тем" to Icons.Outlined.Forum)
            selectedDiscussion?.let {
                if (it.isClosed) add("Тема закрыта" to Icons.Outlined.TaskAlt)
                else add("${it.unreadCount} unread" to Icons.Outlined.MarkChatUnread)
            }
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                Text(if (selectedDiscussion == null) "Назад" else "К списку")
            }
            OutlinedButton(onClick = onRefresh) {
                Text("Обновить")
            }
        }
    }
}

@Composable
private fun NewDiscussionCard(
    title: String,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit,
) {
    AppSectionCard(
        title = "Новая тема",
        hint = "Создай отдельную ветку вместо смешивания разных разговоров",
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Название темы") },
        )
        Button(onClick = onCreate, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Создать тему")
        }
    }
}

@Composable
private fun DiscussionHeaderCard(
    discussion: DiscussionDto,
    draftTitle: String,
    isMutating: Boolean,
    onTitleChange: (String) -> Unit,
    onRename: () -> Unit,
    onToggleClosed: () -> Unit,
    onDelete: () -> Unit,
) {
    AppSectionCard(
        title = "Управление темой",
        hint = "Минимум действий, только реально полезные операции",
    ) {
        OutlinedTextField(
            value = draftTitle,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Название темы") },
            enabled = !isMutating,
            singleLine = true,
        )
        val meta = listOfNotNull(
            discussion.authorName.takeIf { it.isNotBlank() },
            discussion.lastMessageAt,
            if (discussion.isClosed) "Закрыта" else "Открыта",
        ).joinToString(" • ")
        if (meta.isNotBlank()) {
            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onRename,
                enabled = !isMutating && draftTitle.isNotBlank() && draftTitle != discussion.title,
            ) {
                Text(if (isMutating) "Сохранение..." else "Сохранить")
            }
            OutlinedButton(onClick = onToggleClosed, enabled = !isMutating) {
                Text(if (discussion.isClosed) "Открыть тему" else "Закрыть тему")
            }
            OutlinedButton(onClick = onDelete, enabled = !isMutating) {
                Text("Удалить")
            }
        }
    }
}

@Composable
private fun DiscussionRow(
    item: DiscussionDto,
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
                item.kind.takeIf { it.isNotBlank() },
                item.authorName.takeIf { it.isNotBlank() },
                item.lastMessageAt,
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusBadge("Сообщений ${item.messageCount}")
                if (item.unreadCount > 0) {
                    StatusBadge("Непрочитано ${item.unreadCount}")
                }
                if (item.isClosed) {
                    StatusBadge("Закрыта")
                }
            }
        }
    }
}

@Composable
private fun MessageRow(
    item: DiscussionMessageDto,
    canEdit: Boolean,
    isMutating: Boolean,
    onOpenNestedDiscussion: (String) -> Unit,
    onCreateNestedDiscussion: (String) -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by remember(item.id) { mutableStateOf(false) }
    var draftContent by remember(item.id, item.content) { mutableStateOf(item.content) }
    var isCreatingNested by remember(item.id) { mutableStateOf(false) }
    var nestedTitle by remember(item.id) { mutableStateOf("") }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                listOfNotNull(item.authorName.takeIf { it.isNotBlank() }, item.createdAt.takeIf { it.isNotBlank() }).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isEditing) {
                OutlinedTextField(
                    value = draftContent,
                    onValueChange = { draftContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isMutating,
                    label = { Text("Сообщение") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onUpdate(draftContent.trim())
                            isEditing = false
                        },
                        enabled = !isMutating && draftContent.isNotBlank() && draftContent != item.content,
                    ) {
                        Text(if (isMutating) "Сохранение..." else "Сохранить")
                    }
                    OutlinedButton(
                        onClick = {
                            draftContent = item.content
                            isEditing = false
                        },
                        enabled = !isMutating,
                    ) {
                        Text("Отмена")
                    }
                }
            } else {
                Text(item.content, style = MaterialTheme.typography.bodyMedium)
            }
            if (item.mentions.isNotEmpty()) {
                val mentionText = item.mentions.joinToString(" • ") { mention ->
                    buildString {
                        append("@")
                        append(mention.userName.ifBlank { mention.mentionedUserId }.trim())
                        if (mention.isRequest) append(" request")
                        if (mention.isResolved) append(" resolved")
                    }
                }
                Text(
                    mentionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item.nestedDiscTitle?.takeIf { it.isNotBlank() }?.let { nested ->
                Surface(
                    modifier = Modifier.clickable { item.nestedDiscId?.let(onOpenNestedDiscussion) },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ) {
                    Text(
                        text = "Вложенное обсуждение: $nested (${item.nestedDiscMsgCount})",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            if (isCreatingNested) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = nestedTitle,
                            onValueChange = { nestedTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isMutating,
                            label = { Text("Название вложенной темы") },
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onCreateNestedDiscussion(nestedTitle.trim())
                                    nestedTitle = ""
                                    isCreatingNested = false
                                },
                                enabled = !isMutating && nestedTitle.isNotBlank(),
                            ) {
                                Text(if (isMutating) "Создание..." else "Создать подтему")
                            }
                            OutlinedButton(
                                onClick = {
                                    nestedTitle = ""
                                    isCreatingNested = false
                                },
                                enabled = !isMutating,
                            ) {
                                Text("Отмена")
                            }
                        }
                    }
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (canEdit && !isEditing) {
                    OutlinedButton(
                        onClick = {
                            draftContent = item.content
                            isEditing = true
                        },
                        enabled = !isMutating,
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                        Text("Изменить")
                    }
                }
                if (item.nestedDiscId == null && !isCreatingNested) {
                    OutlinedButton(onClick = { isCreatingNested = true }, enabled = !isMutating) {
                        Icon(Icons.Outlined.SubdirectoryArrowRight, contentDescription = null)
                        Text("Подтема")
                    }
                }
                if (canEdit) {
                    OutlinedButton(onClick = onDelete, enabled = !isMutating) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Text(if (isMutating) "Удаление..." else "Удалить")
                    }
                }
            }
            if (item.isEdited) {
                Text(
                    "Отредактировано",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReplyComposer(
    reply: String,
    workspaceMembers: List<ru.custle.mobile.core.model.WorkspaceMemberDto>,
    selectedMentionUserId: String?,
    sendAsRequest: Boolean,
    isClosed: Boolean,
    onReplyChange: (String) -> Unit,
    onSelectMentionUser: (String?) -> Unit,
    onSetSendAsRequest: (Boolean) -> Unit,
    onSend: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionTitle("Быстрый ответ", "Короткое сообщение, mention и request в одном месте")
            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Сообщение") },
            )
            if (workspaceMembers.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AssistChip(
                        onClick = { onSelectMentionUser(null) },
                        label = { Text(if (selectedMentionUserId == null) "Без mention" else "Сбросить mention") },
                    )
                    workspaceMembers.take(4).forEach { member ->
                        val label = listOfNotNull(member.firstName, member.lastName).joinToString(" ").ifBlank { member.email }
                        AssistChip(
                            onClick = { onSelectMentionUser(member.id) },
                            label = { Text(if (selectedMentionUserId == member.id) "@${label.take(12)}" else label.take(12)) },
                        )
                    }
                }
                if (selectedMentionUserId != null) {
                    AssistChip(
                        onClick = { onSetSendAsRequest(!sendAsRequest) },
                        label = { Text(if (sendAsRequest) "Request включён" else "Request выключен") },
                        leadingIcon = { Icon(Icons.Outlined.TaskAlt, contentDescription = null) },
                    )
                }
            }
            Button(
                onClick = onSend,
                enabled = reply.isNotBlank() && !isClosed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isClosed) "Тема закрыта" else "Отправить сообщение")
            }
        }
    }
}

@Composable
private fun SectionBlock(
    title: String,
    hint: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                SectionTitle(title, hint)
                content()
            },
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    hint: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyBlock(
    title: String,
    hint: String,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DiscussionMetaChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
