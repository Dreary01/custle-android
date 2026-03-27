package ru.custle.mobile.feature.mentions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.model.DiscussionDto
import ru.custle.mobile.core.model.DiscussionMessageDto
import ru.custle.mobile.core.model.MentionDto

data class MentionsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<MentionDto> = emptyList(),
    val resolvingIds: Set<String> = emptySet(),
    val selectedMentionId: String? = null,
    val selectedDiscussion: DiscussionDto? = null,
    val discussionMessages: List<DiscussionMessageDto> = emptyList(),
    val loadingDiscussionId: String? = null,
)

class MentionsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(MentionsUiState())
    val state: StateFlow<MentionsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.mentionRepository.list()
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
            )
        }
    }

    fun resolve(id: String) {
        launchCatching {
            _state.value = _state.value.copy(resolvingIds = _state.value.resolvingIds + id, errorMessage = null)
            container.mentionRepository.resolve(id)
            val items = container.mentionRepository.list()
            _state.value = _state.value.copy(
                resolvingIds = _state.value.resolvingIds - id,
                items = items,
            )
        }
    }

    fun openDiscussion(mention: MentionDto) {
        val objectId = mention.objectId?.trim().orEmpty()
        if (objectId.isBlank()) {
            _state.value = _state.value.copy(
                errorMessage = "У этого mention нет object_id, открыть обсуждение нельзя.",
            )
            return
        }

        launchCatching {
            _state.value = _state.value.copy(
                errorMessage = null,
                loadingDiscussionId = mention.id,
                selectedMentionId = mention.id,
                selectedDiscussion = null,
                discussionMessages = emptyList(),
            )

            val discussions = container.discussionRepository.list(objectId)
            val discussion = pickDiscussion(discussions, mention.discussionTitle)
            if (discussion == null) {
                _state.value = _state.value.copy(
                    loadingDiscussionId = null,
                    errorMessage = "Не нашёл обсуждение по теме «${mention.discussionTitle}».",
                )
                return@launchCatching
            }

            val messages = container.discussionRepository.messages(discussion.id)
            _state.value = _state.value.copy(
                loadingDiscussionId = null,
                selectedDiscussion = discussion,
                discussionMessages = messages,
            )
        }
    }

    fun closeDiscussion() {
        _state.value = _state.value.copy(
            selectedMentionId = null,
            selectedDiscussion = null,
            discussionMessages = emptyList(),
            loadingDiscussionId = null,
        )
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        resolvingIds = emptySet(),
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }

    private fun pickDiscussion(items: List<DiscussionDto>, title: String): DiscussionDto? {
        if (items.isEmpty()) return null
        val normalizedTitle = title.normalizeKey()
        return items.firstOrNull { it.title.normalizeKey() == normalizedTitle }
            ?: items.firstOrNull { normalizedTitle.isNotBlank() && it.title.normalizeKey().contains(normalizedTitle) }
            ?: if (items.size == 1) items.first() else null
    }

    private fun String.normalizeKey(): String =
        trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
}

class MentionsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MentionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MentionsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
