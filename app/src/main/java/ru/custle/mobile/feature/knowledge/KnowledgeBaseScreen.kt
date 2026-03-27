@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.knowledge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.ArticleDto
import ru.custle.mobile.core.model.NoteDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun KnowledgeBaseScreen(
    state: KnowledgeUiState,
    onTabChange: (KnowledgeTab) -> Unit,
    onNotesFilterChange: (NotesFilter) -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenArticle: (String) -> Unit,
    onStartCreateNote: () -> Unit,
    onStartEditNote: () -> Unit,
    onUpdateNoteDraft: (title: String?, content: String?, tags: String?, isPrivate: Boolean?) -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onStartCreateArticle: () -> Unit,
    onStartEditArticle: () -> Unit,
    onUpdateArticleDraft: (title: String?, content: String?, category: String?, tags: String?, isPublished: Boolean?) -> Unit,
    onSaveArticle: () -> Unit,
    onDeleteArticle: () -> Unit,
    onCancelEditor: () -> Unit,
    onCloseDetail: () -> Unit,
    onRefresh: () -> Unit,
) {
    var confirmDeleteNote by rememberSaveable(state.selectedNote?.id) { mutableStateOf(false) }
    var confirmDeleteArticle by rememberSaveable(state.selectedArticle?.id) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                KnowledgeTopBar(state = state, onTabChange = onTabChange)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (state.tab) {
                KnowledgeTab.NOTES -> when {
                    state.noteDraft != null -> NoteEditorScreen(
                        draft = state.noteDraft,
                        isSaving = state.isSaving,
                        errorMessage = state.errorMessage,
                        onBack = onCancelEditor,
                        onSave = onSaveNote,
                        onTitleChange = { onUpdateNoteDraft(it, null, null, null) },
                        onContentChange = { onUpdateNoteDraft(null, it, null, null) },
                        onTagsChange = { onUpdateNoteDraft(null, null, it, null) },
                        onPrivacyChange = { onUpdateNoteDraft(null, null, null, it) },
                    )
                    state.selectedNote != null -> NoteDetailScreen(
                        note = state.selectedNote,
                        isSaving = state.isSaving,
                        onBack = onCloseDetail,
                        onEdit = onStartEditNote,
                        onDelete = { confirmDeleteNote = true },
                    )
                    else -> NotesListScreen(
                        notes = state.notes,
                        filter = state.notesFilter,
                        onFilterChange = onNotesFilterChange,
                        onOpenNote = onOpenNote,
                        onCreate = onStartCreateNote,
                        onRefresh = onRefresh,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                    )
                }
                KnowledgeTab.ARTICLES -> when {
                    state.articleDraft != null -> ArticleEditorScreen(
                        draft = state.articleDraft,
                        isSaving = state.isSaving,
                        errorMessage = state.errorMessage,
                        onBack = onCancelEditor,
                        onSave = onSaveArticle,
                        onTitleChange = { onUpdateArticleDraft(it, null, null, null, null) },
                        onContentChange = { onUpdateArticleDraft(null, it, null, null, null) },
                        onCategoryChange = { onUpdateArticleDraft(null, null, it, null, null) },
                        onTagsChange = { onUpdateArticleDraft(null, null, null, it, null) },
                        onPublishedChange = { onUpdateArticleDraft(null, null, null, null, it) },
                    )
                    state.selectedArticle != null -> ArticleDetailScreen(
                        article = state.selectedArticle,
                        isSaving = state.isSaving,
                        onBack = onCloseDetail,
                        onEdit = onStartEditArticle,
                        onDelete = { confirmDeleteArticle = true },
                    )
                    else -> ArticlesListScreen(
                        articles = state.articles,
                        onOpenArticle = onOpenArticle,
                        onCreate = onStartCreateArticle,
                        onRefresh = onRefresh,
                        isLoading = state.isLoading,
                        errorMessage = state.errorMessage,
                    )
                }
            }
        }
    }

    if (confirmDeleteNote && state.selectedNote != null) {
        DestructiveConfirmDialog(
            title = "Удалить заметку?",
            message = "Заметка \"${state.selectedNote.title}\" будет удалена из базы знаний.",
            isBusy = state.isSaving,
            onConfirm = {
                onDeleteNote()
                confirmDeleteNote = false
            },
            onDismiss = { confirmDeleteNote = false },
        )
    }

    if (confirmDeleteArticle && state.selectedArticle != null) {
        DestructiveConfirmDialog(
            title = "Удалить статью?",
            message = "Статья \"${state.selectedArticle.title}\" будет удалена. Это лучше подтверждать отдельно.",
            isBusy = state.isSaving,
            onConfirm = {
                onDeleteArticle()
                confirmDeleteArticle = false
            },
            onDismiss = { confirmDeleteArticle = false },
        )
    }
}

@Composable
private fun KnowledgeTopBar(
    state: KnowledgeUiState,
    onTabChange: (KnowledgeTab) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppHeroCard(
            title = "База знаний",
            subtitle = "Мобильное пространство для заметок и статей. Здесь важно быстро читать, фиксировать мысли и не теряться между режимами.",
            chips = listOf(
                "${state.notes.size} заметок" to Icons.Outlined.AutoStories,
                "${state.articles.size} статей" to Icons.AutoMirrored.Outlined.Article,
                (if (state.tab == KnowledgeTab.NOTES) "Режим заметок" else "Режим статей") to
                    (if (state.tab == KnowledgeTab.NOTES) Icons.Outlined.Lock else Icons.Outlined.Public),
            ),
        )
        TabRow(selectedTabIndex = state.tab.ordinal) {
            Tab(
                selected = state.tab == KnowledgeTab.NOTES,
                onClick = { onTabChange(KnowledgeTab.NOTES) },
                text = { Text("Заметки") },
                icon = { Icon(Icons.Outlined.AutoStories, contentDescription = null) },
            )
            Tab(
                selected = state.tab == KnowledgeTab.ARTICLES,
                onClick = { onTabChange(KnowledgeTab.ARTICLES) },
                text = { Text("Статьи") },
                icon = { Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun NotesListScreen(
    notes: List<NoteDto>,
    filter: NotesFilter,
    onFilterChange: (NotesFilter) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    val filtered = when (filter) {
        NotesFilter.PRIVATE -> notes.filter { it.isPrivate }
        NotesFilter.SHARED -> notes.filter { !it.isPrivate }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KnowledgeToolbar(
            isLoading = isLoading,
            errorMessage = errorMessage,
            createLabel = "Новая заметка",
            onCreate = onCreate,
            onRefresh = onRefresh,
        )
        TabRow(selectedTabIndex = filter.ordinal) {
            Tab(
                selected = filter == NotesFilter.PRIVATE,
                onClick = { onFilterChange(NotesFilter.PRIVATE) },
                text = { Text("Личные") },
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            )
            Tab(
                selected = filter == NotesFilter.SHARED,
                onClick = { onFilterChange(NotesFilter.SHARED) },
                text = { Text("Общие") },
                icon = { Icon(Icons.Outlined.Public, contentDescription = null) },
            )
        }
        if (filtered.isEmpty()) {
            EmptyState("Пока нет заметок")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { note ->
                    NoteCard(note = note, onOpenNote = onOpenNote)
                }
            }
        }
    }
}

@Composable
private fun ArticlesListScreen(
    articles: List<ArticleDto>,
    onOpenArticle: (String) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KnowledgeToolbar(
            isLoading = isLoading,
            errorMessage = errorMessage,
            createLabel = "Новая статья",
            onCreate = onCreate,
            onRefresh = onRefresh,
        )
        if (articles.isEmpty()) {
            EmptyState("Пока нет статей")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(articles, key = { it.id }) { article ->
                    ArticleCard(article = article, onOpenArticle = onOpenArticle)
                }
            }
        }
    }
}

@Composable
private fun KnowledgeToolbar(
    isLoading: Boolean,
    errorMessage: String?,
    createLabel: String,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(errorMessage)
        }
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isLoading) "Обновление списка..." else "Открой карточку, чтобы читать или редактировать",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onCreate) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Text(createLabel)
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Обновить")
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteDto,
    onOpenNote: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpenNote(note.id) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ItemHeader(
                title = note.title,
                subtitle = if (note.isPrivate) "Личная заметка" else "Общая заметка",
                trailing = formatDate(note.updatedAt),
            )
            BodyPreview(note.content, note.contentJson)
            TagsText(note.tags)
        }
    }
}

@Composable
private fun ArticleCard(
    article: ArticleDto,
    onOpenArticle: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpenArticle(article.id) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ItemHeader(
                title = article.title,
                subtitle = article.category.ifBlank { if (article.isPublished) "Опубликовано" else "Черновик" },
                trailing = formatDate(article.updatedAt),
            )
            BodyPreview(article.content, article.contentJson)
            TagsText(article.tags)
        }
    }
}

@Composable
private fun NoteDetailScreen(
    note: NoteDto,
    isSaving: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    DetailScreen(
        title = note.title,
        subtitle = if (note.isPrivate) "Личная заметка" else "Общая заметка",
        metadata = listOf(
            "Создано: ${formatDate(note.createdAt)}",
            "Обновлено: ${formatDate(note.updatedAt)}",
            note.createdBy?.let { "Автор: $it" },
        ),
        tags = note.tags,
        body = note.content,
        contentJson = note.contentJson,
        onBack = onBack,
        onEdit = onEdit,
        onDelete = onDelete,
        isSaving = isSaving,
    )
}

@Composable
private fun ArticleDetailScreen(
    article: ArticleDto,
    isSaving: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    DetailScreen(
        title = article.title,
        subtitle = article.category.ifBlank { if (article.isPublished) "Опубликовано" else "Черновик" },
        metadata = listOf(
            "Создано: ${formatDate(article.createdAt)}",
            "Обновлено: ${formatDate(article.updatedAt)}",
            article.createdBy?.let { "Автор: $it" },
        ),
        tags = article.tags,
        body = article.content,
        contentJson = article.contentJson,
        onBack = onBack,
        onEdit = onEdit,
        onDelete = onDelete,
        isSaving = isSaving,
    )
}

@Composable
private fun DetailScreen(
    title: String,
    subtitle: String,
    metadata: List<String?>,
    tags: String,
    body: String,
    contentJson: JsonElement?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isSaving: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppHeroCard(
                    title = title,
                    subtitle = subtitle,
                    chips = listOf(
                        "Изменить" to Icons.Outlined.Edit,
                        "Удалить" to Icons.Outlined.Delete,
                    ),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                            Text("Назад")
                        }
                        Button(onClick = onEdit, enabled = !isSaving) {
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                            Text("Изменить")
                        }
                        OutlinedButton(onClick = onDelete, enabled = !isSaving) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                            Text(if (isSaving) "Удаление..." else "Удалить")
                        }
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSectionCard(title = "Метаданные") {
                    metadata.filterNotNull().forEach { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                    TagsText(tags)
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSectionCard(title = "Содержимое") {
                    val display = body.ifBlank { contentJson?.toString().orEmpty() }
                    if (display.isBlank()) {
                        Text(
                            "Пока без содержимого",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(display, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorScreen(
    draft: NoteDraft,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onPrivacyChange: (Boolean) -> Unit,
) {
    EditorScreen(
        title = if (draft.id == null) "Новая заметка" else "Редактирование заметки",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onBack = onBack,
        onSave = onSave,
        canSave = draft.title.isNotBlank(),
    ) {
        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Заголовок") },
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.content,
            onValueChange = onContentChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Содержимое") },
            minLines = 8,
        )
        OutlinedTextField(
            value = draft.tags,
            onValueChange = onTagsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Теги") },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Приватная заметка", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (draft.isPrivate) "Видна только автору" else "Доступна в workspace",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = draft.isPrivate, onCheckedChange = onPrivacyChange)
        }
    }
}

@Composable
private fun ArticleEditorScreen(
    draft: ArticleDraft,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onPublishedChange: (Boolean) -> Unit,
) {
    EditorScreen(
        title = if (draft.id == null) "Новая статья" else "Редактирование статьи",
        isSaving = isSaving,
        errorMessage = errorMessage,
        onBack = onBack,
        onSave = onSave,
        canSave = draft.title.isNotBlank(),
    ) {
        OutlinedTextField(
            value = draft.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Заголовок") },
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.category,
            onValueChange = onCategoryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Категория") },
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.content,
            onValueChange = onContentChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Содержимое") },
            minLines = 8,
        )
        OutlinedTextField(
            value = draft.tags,
            onValueChange = onTagsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Теги") },
            singleLine = true,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Публикация", style = MaterialTheme.typography.titleSmall)
                Text(
                    if (draft.isPublished) "Статья опубликована" else "Черновик",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = draft.isPublished, onCheckedChange = onPublishedChange)
        }
    }
}

@Composable
private fun EditorScreen(
    title: String,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    content: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppHeroCard(
                    title = title,
                    subtitle = "Редактор должен ощущаться как спокойная рабочая форма, а не как служебная админка.",
                    chips = listOf("Сохранить" to Icons.Outlined.Add),
                ) {
                    if (!errorMessage.isNullOrBlank()) {
                        ErrorBanner(errorMessage)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                            Text("Назад")
                        }
                        Button(onClick = onSave, enabled = canSave && !isSaving) {
                            Text(if (isSaving) "Сохранение..." else "Сохранить")
                        }
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSectionCard(title = "Поля") {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ItemHeader(
    title: String,
    subtitle: String,
    trailing: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            text = listOf(subtitle, trailing).filter { it.isNotBlank() }.joinToString(" • "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BodyPreview(
    body: String,
    contentJson: JsonElement?,
) {
    val display = body.ifBlank { contentJson?.toString().orEmpty() }
    if (display.isNotBlank()) {
        Text(
            text = display.take(220),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TagsText(tags: String) {
    if (tags.isBlank()) return
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = "Теги: $tags",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyStateCard(
            title = message,
            message = "Этот раздел заполнится, когда в базе знаний появится контент.",
        )
    }
}

@Composable
private fun KnowledgeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(value).atZone(ZoneId.systemDefault()).format(dateFormatter)
    }.getOrElse { value }
}
