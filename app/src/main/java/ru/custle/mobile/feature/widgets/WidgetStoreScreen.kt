package ru.custle.mobile.feature.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.WidgetCatalogDto
import ru.custle.mobile.core.ui.components.ErrorBanner

data class WidgetStoreUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<WidgetCatalogDto> = emptyList(),
    val mutatingWidgetId: String? = null,
)

class WidgetStoreViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(WidgetStoreUiState())
    val state: StateFlow<WidgetStoreUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.widgetCatalogRepository.list()
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
            )
        }
    }

    fun install(id: String) {
        launchCatching {
            _state.value = _state.value.copy(mutatingWidgetId = id, errorMessage = null)
            container.widgetCatalogRepository.install(id)
            _state.value = _state.value.copy(
                items = container.widgetCatalogRepository.list(),
                mutatingWidgetId = null,
            )
        }
    }

    fun uninstall(id: String) {
        launchCatching {
            _state.value = _state.value.copy(mutatingWidgetId = id, errorMessage = null)
            container.widgetCatalogRepository.uninstall(id)
            _state.value = _state.value.copy(
                items = container.widgetCatalogRepository.list(),
                mutatingWidgetId = null,
            )
        }
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mutatingWidgetId = null,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }
}

class WidgetStoreViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WidgetStoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WidgetStoreViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun WidgetStoreRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { WidgetStoreViewModelFactory(container) }
    val viewModel: WidgetStoreViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    WidgetStoreScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onInstall = viewModel::install,
        onUninstall = viewModel::uninstall,
    )
}

@Composable
fun WidgetStoreScreen(
    state: WidgetStoreUiState,
    onRefresh: () -> Unit,
    onInstall: (String) -> Unit,
    onUninstall: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Виджеты",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Button(onClick = onRefresh) { Text("Обновить") }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                DsCard {
                    Text("Виджеты не найдены", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                DsCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            listOfNotNull(
                                item.category,
                                if (item.isPublished) "published" else "draft",
                                if (item.installed) "installed" else null,
                            ).joinToString(" / "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        item.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (item.installed) {
                                OutlinedButton(
                                    onClick = { onUninstall(item.id) },
                                    enabled = state.mutatingWidgetId != item.id,
                                ) {
                                    Text(if (state.mutatingWidgetId == item.id) "Удаление..." else "Удалить")
                                }
                            } else {
                                Button(
                                    onClick = { onInstall(item.id) },
                                    enabled = state.mutatingWidgetId != item.id,
                                ) {
                                    Text(if (state.mutatingWidgetId == item.id) "Установка..." else "Установить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}
