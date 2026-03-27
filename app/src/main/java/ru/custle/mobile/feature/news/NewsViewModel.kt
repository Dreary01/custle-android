package ru.custle.mobile.feature.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.model.NewsDto

data class NewsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<NewsDto> = emptyList(),
    val selectedNews: NewsDto? = null,
)

class NewsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.newsRepository.list()
            val selectedId = _state.value.selectedNews?.id
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
                selectedNews = items.firstOrNull { it.id == selectedId } ?: items.firstOrNull(),
            )
        }
    }

    fun open(id: String) {
        _state.value = _state.value.copy(selectedNews = _state.value.items.firstOrNull { it.id == id })
    }

    fun close() {
        _state.value = _state.value.copy(selectedNews = null)
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }
}

class NewsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
