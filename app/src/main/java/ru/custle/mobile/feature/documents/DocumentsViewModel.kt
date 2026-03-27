package ru.custle.mobile.feature.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.model.DocumentFileDto

data class DocumentsUiState(
    val objectId: String = "",
    val objectName: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val files: List<DocumentFileDto> = emptyList(),
    val indexedDocIds: Set<String> = emptySet(),
    val canUpload: Boolean = false,
    val canRename: Boolean = false,
    val canDelete: Boolean = false,
    val downloadedDocId: String? = null,
    val downloadedPath: String? = null,
    val downloadingDocId: String? = null,
    val uploadingFileName: String? = null,
    val mutatingDocId: String? = null,
    val isReindexing: Boolean = false,
    val queuedForReindex: Int = 0,
) {
    val indexedCount: Int get() = files.count { file -> file.docId in indexedDocIds }
    val totalCount: Int get() = files.size
    val pendingCount: Int get() = files.size - indexedCount
}

class DocumentsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(DocumentsUiState())
    val state: StateFlow<DocumentsUiState> = _state.asStateFlow()
    private var reindexPollJob: Job? = null

    fun load(objectId: String, objectName: String? = null) {
        launchCatching {
            _state.value = _state.value.copy(
                objectId = objectId,
                objectName = objectName,
                isLoading = true,
                errorMessage = null,
                downloadedDocId = null,
                downloadedPath = null,
            )
            val bundle = container.documentRepository.load(objectId)
            _state.value = _state.value.copy(
                isLoading = false,
                files = bundle.files,
                indexedDocIds = bundle.indexStatus.filter { it.indexed }.map { it.docId }.toSet(),
                canUpload = bundle.info.features.upload,
                canRename = bundle.info.features.rename,
                canDelete = bundle.info.features.delete,
            )
            if (_state.value.pendingCount > 0 && !_state.value.isReindexing) {
                startPollingIndexStatus(objectId)
            }
        }
    }

    fun refresh() {
        val objectId = _state.value.objectId
        if (objectId.isBlank()) return
        load(objectId, _state.value.objectName)
    }

    fun download(file: DocumentFileDto) {
        launchCatching {
            _state.value = _state.value.copy(downloadingDocId = file.docId, errorMessage = null)
            val localFile = kotlinx.coroutines.withContext(Dispatchers.IO) {
                container.documentRepository.download(file.docId, fileName(file))
            }
            _state.value = _state.value.copy(
                downloadingDocId = null,
                downloadedDocId = file.docId,
                downloadedPath = localFile.absolutePath,
            )
        }
    }

    fun upload(uri: Uri) {
        val objectId = _state.value.objectId
        if (objectId.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(uploadingFileName = uri.lastPathSegment, errorMessage = null)
            kotlinx.coroutines.withContext(Dispatchers.IO) {
                container.documentRepository.upload(objectId, uri)
            }
            val bundle = container.documentRepository.load(objectId)
            _state.value = _state.value.copy(
                files = bundle.files,
                indexedDocIds = bundle.indexStatus.filter { it.indexed }.map { it.docId }.toSet(),
                canUpload = bundle.info.features.upload,
                canRename = bundle.info.features.rename,
                canDelete = bundle.info.features.delete,
                uploadingFileName = null,
            )
        }
    }

    fun rename(file: DocumentFileDto, newName: String) {
        val objectId = _state.value.objectId
        if (objectId.isBlank() || newName.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(mutatingDocId = file.docId, errorMessage = null)
            container.documentRepository.rename(objectId, file.docId, newName.trim())
            val bundle = container.documentRepository.load(objectId)
            _state.value = _state.value.copy(
                files = bundle.files,
                indexedDocIds = bundle.indexStatus.filter { it.indexed }.map { it.docId }.toSet(),
                canUpload = bundle.info.features.upload,
                canRename = bundle.info.features.rename,
                canDelete = bundle.info.features.delete,
                mutatingDocId = null,
            )
        }
    }

    fun delete(file: DocumentFileDto) {
        val objectId = _state.value.objectId
        if (objectId.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(mutatingDocId = file.docId, errorMessage = null)
            container.documentRepository.delete(objectId, file.docId)
            val bundle = container.documentRepository.load(objectId)
            _state.value = _state.value.copy(
                files = bundle.files,
                indexedDocIds = bundle.indexStatus.filter { it.indexed }.map { it.docId }.toSet(),
                canUpload = bundle.info.features.upload,
                canRename = bundle.info.features.rename,
                canDelete = bundle.info.features.delete,
                downloadedDocId = if (_state.value.downloadedDocId == file.docId) null else _state.value.downloadedDocId,
                downloadedPath = if (_state.value.downloadedDocId == file.docId) null else _state.value.downloadedPath,
                mutatingDocId = null,
            )
        }
    }

    fun reindex() {
        val objectId = _state.value.objectId
        if (objectId.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isReindexing = true, errorMessage = null)
            val queued = container.documentRepository.reindex(objectId)
            _state.value = _state.value.copy(queuedForReindex = queued, isReindexing = queued > 0)
            if (queued > 0) {
                startPollingIndexStatus(objectId)
            } else {
                refresh()
            }
        }
    }

    private fun startPollingIndexStatus(objectId: String) {
        reindexPollJob?.cancel()
        reindexPollJob = viewModelScope.launch {
            repeat(20) {
                delay(3000)
                val bundle = runCatching { container.documentRepository.load(objectId) }.getOrNull() ?: return@launch
                val indexedDocIds = bundle.indexStatus.filter { it.indexed }.map { it.docId }.toSet()
                val pending = bundle.files.count { it.docId !in indexedDocIds }
                _state.value = _state.value.copy(
                    files = bundle.files,
                    indexedDocIds = indexedDocIds,
                    canUpload = bundle.info.features.upload,
                    canRename = bundle.info.features.rename,
                    canDelete = bundle.info.features.delete,
                    queuedForReindex = pending,
                    isReindexing = pending > 0,
                )
                if (pending == 0) return@launch
            }
            _state.value = _state.value.copy(isReindexing = false)
        }
    }

    private fun fileName(file: DocumentFileDto): String = file.id.substringAfterLast('/').ifBlank {
        file.docId
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        downloadingDocId = null,
                        uploadingFileName = null,
                        mutatingDocId = null,
                        isReindexing = false,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }

    override fun onCleared() {
        reindexPollJob?.cancel()
        super.onCleared()
    }
}

class DocumentsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocumentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocumentsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
