package ru.custle.mobile.feature.discussions

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
import ru.custle.mobile.core.model.DiscussionDto
import ru.custle.mobile.core.model.DiscussionMessageDto
import ru.custle.mobile.core.model.WorkspaceMemberDto

data class DiscussionsUiState(
    val objectId: String = "",
    val objectName: String = "",
    val isLoading: Boolean = false,
    val isLoadingMessages: Boolean = false,
    val errorMessage: String? = null,
    val discussions: List<DiscussionDto> = emptyList(),
    val selectedDiscussion: DiscussionDto? = null,
    val nestedDiscussions: List<DiscussionDto> = emptyList(),
    val messages: List<DiscussionMessageDto> = emptyList(),
    val workspaceMembers: List<WorkspaceMemberDto> = emptyList(),
    val currentUserId: String? = null,
    val selectedMentionUserId: String? = null,
    val sendAsRequest: Boolean = false,
    val mutatingDiscussionId: String? = null,
    val mutatingMessageId: String? = null,
)

class DiscussionsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(DiscussionsUiState())
    val state: StateFlow<DiscussionsUiState> = _state.asStateFlow()

    fun loadObject(objectId: String, objectName: String) {
        if (_state.value.objectId == objectId && _state.value.discussions.isNotEmpty()) return
        launchCatching {
            _state.value = DiscussionsUiState(objectId = objectId, objectName = objectName, isLoading = true)
            val payload = coroutineScope {
                val discussions = async { container.discussionRepository.list(objectId) }
                val workspaceMembers = async { runCatching { container.profileRepository.workspaceMembers() }.getOrDefault(emptyList()) }
                val currentUser = async { runCatching { container.authRepository.me() }.getOrNull() }
                Triple(discussions.await(), workspaceMembers.await(), currentUser.await())
            }
            _state.value = _state.value.copy(
                isLoading = false,
                discussions = payload.first,
                workspaceMembers = payload.second,
                currentUserId = payload.third?.id,
            )
        }
    }

    fun refresh() {
        val objectId = _state.value.objectId
        if (objectId.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val discussions = container.discussionRepository.list(objectId)
            val selectedId = _state.value.selectedDiscussion?.id
            val selected = discussions.firstOrNull { it.id == selectedId }
                ?: _state.value.nestedDiscussions.firstOrNull { it.id == selectedId }
                ?: _state.value.selectedDiscussion?.takeIf { it.id == selectedId }
            _state.value = _state.value.copy(
                isLoading = false,
                discussions = discussions,
                selectedDiscussion = selected,
            )
            if (selected != null) {
                loadMessages(selected.id)
            } else {
                _state.value = _state.value.copy(nestedDiscussions = emptyList(), messages = emptyList())
            }
        }
    }

    fun openDiscussion(discussionId: String) {
        val selected = _state.value.discussions.firstOrNull { it.id == discussionId }
            ?: _state.value.nestedDiscussions.firstOrNull { it.id == discussionId }
            ?: _state.value.selectedDiscussion?.takeIf { it.id == discussionId }
        _state.value = _state.value.copy(selectedDiscussion = selected, messages = emptyList(), nestedDiscussions = emptyList())
        loadMessages(discussionId)
    }

    fun closeDiscussion() {
        _state.value = _state.value.copy(selectedDiscussion = null, messages = emptyList(), nestedDiscussions = emptyList())
    }

    fun createDiscussion(title: String) {
        val objectId = _state.value.objectId
        if (objectId.isBlank() || title.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val discussionId = container.discussionRepository.createDiscussion(objectId, title)
            val discussions = container.discussionRepository.list(objectId)
            _state.value = _state.value.copy(
                isLoading = false,
                discussions = discussions,
                selectedDiscussion = discussions.firstOrNull { it.id == discussionId },
            )
            if (discussionId.isNotBlank()) {
                loadMessages(discussionId)
            }
        }
    }

    fun renameDiscussion(title: String) {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        if (title.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(mutatingDiscussionId = discussionId, errorMessage = null)
            container.discussionRepository.updateDiscussion(discussionId = discussionId, title = title.trim())
            val discussions = container.discussionRepository.list(_state.value.objectId)
            val selected = discussions.firstOrNull { it.id == discussionId }
                ?: _state.value.selectedDiscussion?.copy(title = title.trim())
            _state.value = _state.value.copy(
                discussions = discussions,
                selectedDiscussion = selected,
                mutatingDiscussionId = null,
            )
        }
    }

    fun setDiscussionClosed(closed: Boolean) {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        launchCatching {
            _state.value = _state.value.copy(mutatingDiscussionId = discussionId, errorMessage = null)
            container.discussionRepository.updateDiscussion(discussionId = discussionId, isClosed = closed)
            val discussions = container.discussionRepository.list(_state.value.objectId)
            val selected = discussions.firstOrNull { it.id == discussionId }
                ?: _state.value.selectedDiscussion?.copy(isClosed = closed)
            _state.value = _state.value.copy(
                discussions = discussions,
                selectedDiscussion = selected,
                mutatingDiscussionId = null,
            )
        }
    }

    fun deleteDiscussion() {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        launchCatching {
            _state.value = _state.value.copy(mutatingDiscussionId = discussionId, errorMessage = null)
            container.discussionRepository.deleteDiscussion(discussionId)
            val discussions = container.discussionRepository.list(_state.value.objectId)
            _state.value = _state.value.copy(
                discussions = discussions,
                selectedDiscussion = null,
                nestedDiscussions = emptyList(),
                messages = emptyList(),
                mutatingDiscussionId = null,
            )
        }
    }

    fun selectMentionUser(userId: String?) {
        _state.value = _state.value.copy(selectedMentionUserId = userId)
    }

    fun setSendAsRequest(enabled: Boolean) {
        _state.value = _state.value.copy(sendAsRequest = enabled)
    }

    fun sendMessage(content: String) {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        if (content.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isLoadingMessages = true, errorMessage = null)
            val mentionedUserIds = listOfNotNull(_state.value.selectedMentionUserId)
            container.discussionRepository.createMessage(
                discussionId = discussionId,
                content = content,
                mentionedUserIds = mentionedUserIds,
                isRequest = _state.value.sendAsRequest && mentionedUserIds.isNotEmpty(),
            )
            val messages = container.discussionRepository.messages(discussionId)
            val discussions = container.discussionRepository.list(_state.value.objectId)
            _state.value = _state.value.copy(
                isLoadingMessages = false,
                messages = messages,
                discussions = discussions,
                selectedDiscussion = discussions.firstOrNull { it.id == discussionId } ?: _state.value.selectedDiscussion,
                selectedMentionUserId = null,
                sendAsRequest = false,
            )
        }
    }

    fun updateMessage(messageId: String, content: String) {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        if (content.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(mutatingMessageId = messageId, errorMessage = null)
            container.discussionRepository.updateMessage(messageId, content.trim())
            val messages = container.discussionRepository.messages(discussionId)
            _state.value = _state.value.copy(
                messages = messages,
                mutatingMessageId = null,
            )
        }
    }

    fun createNestedDiscussion(parentMessageId: String, title: String) {
        val objectId = _state.value.objectId
        val parentDiscussionId = _state.value.selectedDiscussion?.id ?: return
        if (objectId.isBlank() || title.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(mutatingMessageId = parentMessageId, errorMessage = null)
            val nestedDiscussionId = container.discussionRepository.createNestedDiscussion(
                objectId = objectId,
                title = title.trim(),
                parentDiscussionId = parentDiscussionId,
                parentMessageId = parentMessageId,
            )
            val discussions = container.discussionRepository.list(objectId)
            _state.value = _state.value.copy(
                discussions = discussions,
                mutatingMessageId = null,
            )
            if (nestedDiscussionId.isNotBlank()) {
                openDiscussion(nestedDiscussionId)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        val discussionId = _state.value.selectedDiscussion?.id ?: return
        launchCatching {
            _state.value = _state.value.copy(mutatingMessageId = messageId, errorMessage = null)
            container.discussionRepository.deleteMessage(messageId)
            val messages = container.discussionRepository.messages(discussionId)
            val discussions = container.discussionRepository.list(_state.value.objectId)
            _state.value = _state.value.copy(
                messages = messages,
                discussions = discussions,
                selectedDiscussion = discussions.firstOrNull { it.id == discussionId } ?: _state.value.selectedDiscussion,
                mutatingMessageId = null,
            )
        }
    }

    private fun loadMessages(discussionId: String) {
        launchCatching {
            _state.value = _state.value.copy(isLoadingMessages = true, errorMessage = null)
            val payload = coroutineScope {
                val messages = async { container.discussionRepository.messages(discussionId) }
                val nested = async { runCatching { container.discussionRepository.nested(discussionId) }.getOrDefault(emptyList()) }
                Pair(messages.await(), nested.await())
            }
            _state.value = _state.value.copy(
                isLoadingMessages = false,
                messages = payload.first,
                nestedDiscussions = payload.second,
            )
        }
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isLoadingMessages = false,
                        mutatingDiscussionId = null,
                        mutatingMessageId = null,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }
}

class DiscussionsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscussionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiscussionsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
