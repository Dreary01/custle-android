package ru.custle.mobile.feature.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer

class KnowledgeBaseViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(KnowledgeUiState())
    val state: StateFlow<KnowledgeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: KnowledgeTab) {
        _state.value = _state.value.copy(
            tab = tab,
            selectedNote = null,
            selectedArticle = null,
            noteDraft = null,
            articleDraft = null,
        )
    }

    fun selectNotesFilter(filter: NotesFilter) {
        _state.value = _state.value.copy(notesFilter = filter)
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val (notes, articles) = coroutineScope {
                val notesDeferred = async { container.knowledgeRepository.notes() }
                val articlesDeferred = async { container.knowledgeRepository.articles() }
                notesDeferred.await() to articlesDeferred.await()
            }
            _state.value = _state.value.copy(
                isLoading = false,
                notes = notes,
                articles = articles,
            )
        }
    }

    fun openNote(id: String) {
        launchCatching {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                tab = KnowledgeTab.NOTES,
                noteDraft = null,
                articleDraft = null,
            )
            val note = container.knowledgeRepository.note(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selectedNote = note,
                selectedArticle = null,
            )
        }
    }

    fun openArticle(id: String) {
        launchCatching {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = null,
                tab = KnowledgeTab.ARTICLES,
                noteDraft = null,
                articleDraft = null,
            )
            val article = container.knowledgeRepository.article(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selectedArticle = article,
                selectedNote = null,
            )
        }
    }

    fun closeDetail() {
        _state.value = _state.value.copy(
            selectedNote = null,
            selectedArticle = null,
            noteDraft = null,
            articleDraft = null,
        )
    }

    fun startCreateNote() {
        _state.value = _state.value.copy(
            tab = KnowledgeTab.NOTES,
            selectedNote = null,
            selectedArticle = null,
            articleDraft = null,
            noteDraft = NoteDraft(),
        )
    }

    fun startEditNote() {
        val note = _state.value.selectedNote ?: return
        _state.value = _state.value.copy(
            noteDraft = NoteDraft(
                id = note.id,
                title = note.title,
                content = note.content,
                tags = note.tags,
                isPrivate = note.isPrivate,
            ),
            articleDraft = null,
        )
    }

    fun updateNoteDraft(
        title: String? = null,
        content: String? = null,
        tags: String? = null,
        isPrivate: Boolean? = null,
    ) {
        val draft = _state.value.noteDraft ?: return
        _state.value = _state.value.copy(
            noteDraft = draft.copy(
                title = title ?: draft.title,
                content = content ?: draft.content,
                tags = tags ?: draft.tags,
                isPrivate = isPrivate ?: draft.isPrivate,
            ),
        )
    }

    fun saveNote() {
        val draft = _state.value.noteDraft ?: return
        launchCatching {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            val note = if (draft.id == null) {
                container.knowledgeRepository.createNote(
                    title = draft.title.trim(),
                    content = draft.content,
                    tags = draft.tags,
                    isPrivate = draft.isPrivate,
                )
            } else {
                container.knowledgeRepository.updateNote(
                    id = draft.id,
                    title = draft.title.trim(),
                    content = draft.content,
                    tags = draft.tags,
                    isPrivate = draft.isPrivate,
                )
            }
            val notes = container.knowledgeRepository.notes()
            _state.value = _state.value.copy(
                isSaving = false,
                notes = notes,
                selectedNote = note,
                noteDraft = null,
            )
        }
    }

    fun deleteSelectedNote() {
        val id = _state.value.selectedNote?.id ?: return
        launchCatching {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            container.knowledgeRepository.deleteNote(id)
            val notes = container.knowledgeRepository.notes()
            _state.value = _state.value.copy(
                isSaving = false,
                notes = notes,
                selectedNote = null,
                noteDraft = null,
            )
        }
    }

    fun startCreateArticle() {
        _state.value = _state.value.copy(
            tab = KnowledgeTab.ARTICLES,
            selectedNote = null,
            selectedArticle = null,
            noteDraft = null,
            articleDraft = ArticleDraft(),
        )
    }

    fun startEditArticle() {
        val article = _state.value.selectedArticle ?: return
        _state.value = _state.value.copy(
            articleDraft = ArticleDraft(
                id = article.id,
                title = article.title,
                content = article.content,
                category = article.category,
                tags = article.tags,
                isPublished = article.isPublished,
            ),
            noteDraft = null,
        )
    }

    fun updateArticleDraft(
        title: String? = null,
        content: String? = null,
        category: String? = null,
        tags: String? = null,
        isPublished: Boolean? = null,
    ) {
        val draft = _state.value.articleDraft ?: return
        _state.value = _state.value.copy(
            articleDraft = draft.copy(
                title = title ?: draft.title,
                content = content ?: draft.content,
                category = category ?: draft.category,
                tags = tags ?: draft.tags,
                isPublished = isPublished ?: draft.isPublished,
            ),
        )
    }

    fun saveArticle() {
        val draft = _state.value.articleDraft ?: return
        launchCatching {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            val article = if (draft.id == null) {
                container.knowledgeRepository.createArticle(
                    title = draft.title.trim(),
                    content = draft.content,
                    category = draft.category,
                    tags = draft.tags,
                    isPublished = draft.isPublished,
                )
            } else {
                container.knowledgeRepository.updateArticle(
                    id = draft.id,
                    title = draft.title.trim(),
                    content = draft.content,
                    category = draft.category,
                    tags = draft.tags,
                    isPublished = draft.isPublished,
                )
            }
            val articles = container.knowledgeRepository.articles()
            _state.value = _state.value.copy(
                isSaving = false,
                articles = articles,
                selectedArticle = article,
                articleDraft = null,
            )
        }
    }

    fun deleteSelectedArticle() {
        val id = _state.value.selectedArticle?.id ?: return
        launchCatching {
            _state.value = _state.value.copy(isSaving = true, errorMessage = null)
            container.knowledgeRepository.deleteArticle(id)
            val articles = container.knowledgeRepository.articles()
            _state.value = _state.value.copy(
                isSaving = false,
                articles = articles,
                selectedArticle = null,
                articleDraft = null,
            )
        }
    }

    fun cancelEditor() {
        _state.value = _state.value.copy(noteDraft = null, articleDraft = null)
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSaving = false,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }
}

class KnowledgeBaseViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KnowledgeBaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KnowledgeBaseViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
