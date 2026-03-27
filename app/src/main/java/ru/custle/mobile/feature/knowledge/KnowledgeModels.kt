package ru.custle.mobile.feature.knowledge

import ru.custle.mobile.core.model.ArticleDto
import ru.custle.mobile.core.model.NoteDto

enum class KnowledgeTab {
    NOTES,
    ARTICLES,
}

enum class NotesFilter {
    PRIVATE,
    SHARED,
}

data class KnowledgeUiState(
    val tab: KnowledgeTab = KnowledgeTab.NOTES,
    val notesFilter: NotesFilter = NotesFilter.PRIVATE,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val notes: List<NoteDto> = emptyList(),
    val articles: List<ArticleDto> = emptyList(),
    val selectedNote: NoteDto? = null,
    val selectedArticle: ArticleDto? = null,
    val noteDraft: NoteDraft? = null,
    val articleDraft: ArticleDraft? = null,
)

data class NoteDraft(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val isPrivate: Boolean = true,
)

data class ArticleDraft(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val tags: String = "",
    val isPublished: Boolean = false,
)
