@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.knowledge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.ArticleDto
import ru.custle.mobile.core.model.NoteDto
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog
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
                    .statusBarsPadding(),
            ) {
                TabRow(selectedTabIndex = state.tab.ordinal) {
                    Tab(
                        selected = state.tab == KnowledgeTab.NOTES,
                        onClick = { onTabChange(KnowledgeTab.NOTES) },
                        text = { Text("Заметки") },
                    )
                    Tab(
                        selected = state.tab == KnowledgeTab.ARTICLES,
                        onClick = { onTabChange(KnowledgeTab.ARTICLES) },
                        text = { Text("Статьи") },
                    )
                }
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
            message = "Заметка \"${state.selectedNote.title}\" будет удалена.",
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
            message = "Статья \"${state.selectedArticle.title}\" будет удалена.",
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ListToolbar(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onCreate = onCreate,
                onRefresh = onRefresh,
            )
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter == NotesFilter.PRIVATE,
                    onClick = { onFilterChange(NotesFilter.PRIVATE) },
                    label = { Text("Личные") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                )
                FilterChip(
                    selected = filter == NotesFilter.SHARED,
                    onClick = { onFilterChange(NotesFilter.SHARED) },
                    label = { Text("Общие") },
                    leadingIcon = { Icon(Icons.Outlined.Public, contentDescription = null) },
                )
            }
        }
        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Нет заметок",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { note ->
                NoteCard(note = note, onOpenNote = onOpenNote)
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ListToolbar(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onCreate = onCreate,
                onRefresh = onRefresh,
            )
        }
        if (articles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Нет статей",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(articles, key = { it.id }) { article ->
                ArticleCard(article = article, onOpenArticle = onOpenArticle)
            }
        }
    }
}

@Composable
private fun ListToolbar(
    isLoading: Boolean,
    errorMessage: String?,
    onCreate: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(errorMessage)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                Text(
                    "Загрузка...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
            }
            Button(onClick = onCreate) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteDto,
    onOpenNote: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpenNote(note.id) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                note.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (note.isPrivate) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.height(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    formatDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BodyPreview(note.content, note.contentJson)
            TagsRow(note.tags)
        }
    }
}

@Composable
private fun ArticleCard(
    article: ArticleDto,
    onOpenArticle: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onOpenArticle(article.id) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                article.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val status = article.category.ifBlank {
                    if (article.isPublished) "Опубликовано" else "Черновик"
                }
                Text(
                    status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    formatDate(article.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BodyPreview(article.content, article.contentJson)
            TagsRow(article.tags)
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
        subtitle = if (note.isPrivate) "Личная" else "Общая",
        metadata = listOfNotNull(
            formatDate(note.createdAt).takeIf { it.isNotBlank() },
            formatDate(note.updatedAt).takeIf { it.isNotBlank() },
            note.createdBy,
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
        metadata = listOfNotNull(
            formatDate(article.createdAt).takeIf { it.isNotBlank() },
            formatDate(article.updatedAt).takeIf { it.isNotBlank() },
            article.createdBy,
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onEdit, enabled = !isSaving) {
                        Icon(Icons.Outlined.Edit, contentDescription = null)
                    }
                    IconButton(onClick = onDelete, enabled = !isSaving) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val metaText = metadata.filterNotNull().filter { it.isNotBlank() }
                    if (metaText.isNotEmpty()) {
                        Text(
                            metaText.joinToString(" / "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TagsRow(tags)
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                val display = body.ifBlank { contentJson?.toString().orEmpty() }
                Text(
                    text = display.ifBlank { "\u2014" },
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (display.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
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
        title = if (draft.id == null) "Новая заметка" else "Редактирование",
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
            minLines = 6,
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
            Text(
                "Приватная",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
        title = if (draft.id == null) "Новая статья" else "Редактирование",
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
            minLines = 6,
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
            Text(
                if (draft.isPublished) "Опубликовано" else "Черновик",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onSave, enabled = canSave && !isSaving) {
                    Text(if (isSaving) "..." else "Сохранить")
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ErrorBanner(errorMessage)
                }
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    content()
                }
            }
        }
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
            text = display.take(200),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TagsRow(tags: String) {
    if (tags.isBlank()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.split(",", " ").filter { it.isNotBlank() }.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = tag.trim(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        Instant.parse(value).atZone(ZoneId.systemDefault()).format(dateFormatter)
    }.getOrElse { value }
}
