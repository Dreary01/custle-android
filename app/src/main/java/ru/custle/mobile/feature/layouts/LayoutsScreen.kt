package ru.custle.mobile.feature.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.GridStateDto
import ru.custle.mobile.core.model.WidgetLayoutDto

data class LayoutsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val pageType: String = "dashboard",
    val gridId: String = "documents",
    val objectId: String = "",
    val typeId: String = "",
    val layouts: List<WidgetLayoutDto> = emptyList(),
    val gridState: GridStateDto? = null,
)

class LayoutsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(LayoutsUiState())
    val state: StateFlow<LayoutsUiState> = _state.asStateFlow()

    init {
        loadLayouts()
        loadGridState()
    }

    fun updatePageType(value: String) {
        _state.value = _state.value.copy(pageType = value)
    }

    fun updateGridId(value: String) {
        _state.value = _state.value.copy(gridId = value)
    }

    fun updateObjectId(value: String) {
        _state.value = _state.value.copy(objectId = value)
    }

    fun updateTypeId(value: String) {
        _state.value = _state.value.copy(typeId = value)
    }

    fun applyLayoutPreset(pageType: String) {
        _state.value = _state.value.copy(pageType = pageType)
        loadLayouts()
    }

    fun applyGridPreset(gridId: String) {
        _state.value = _state.value.copy(gridId = gridId)
        loadGridState()
    }

    fun loadLayouts() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val layouts = container.layoutInspectorRepository.widgetLayouts(
                pageType = _state.value.pageType.trim(),
                objectId = _state.value.objectId.trim().ifBlank { null },
                typeId = _state.value.typeId.trim().ifBlank { null },
            )
            _state.value = _state.value.copy(
                isLoading = false,
                layouts = layouts,
            )
        }
    }

    fun loadGridState() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val gridState = container.layoutInspectorRepository.gridState(
                gridId = _state.value.gridId.trim(),
                objectId = _state.value.objectId.trim().ifBlank { null },
                typeId = _state.value.typeId.trim().ifBlank { null },
            )
            _state.value = _state.value.copy(
                isLoading = false,
                gridState = gridState,
            )
        }
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

class LayoutsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LayoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LayoutsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun LayoutsRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { LayoutsViewModelFactory(container) }
    val viewModel: LayoutsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LayoutsScreen(
        state = state,
        onUpdatePageType = viewModel::updatePageType,
        onUpdateGridId = viewModel::updateGridId,
        onUpdateObjectId = viewModel::updateObjectId,
        onUpdateTypeId = viewModel::updateTypeId,
        onLoadLayouts = viewModel::loadLayouts,
        onLoadGridState = viewModel::loadGridState,
        onLayoutPreset = viewModel::applyLayoutPreset,
        onGridPreset = viewModel::applyGridPreset,
    )
}

@Composable
fun LayoutsScreen(
    state: LayoutsUiState,
    onUpdatePageType: (String) -> Unit,
    onUpdateGridId: (String) -> Unit,
    onUpdateObjectId: (String) -> Unit,
    onUpdateTypeId: (String) -> Unit,
    onLoadLayouts: () -> Unit,
    onLoadGridState: () -> Unit,
    onLayoutPreset: (String) -> Unit,
    onGridPreset: (String) -> Unit,
) {
    val json = remember { Json { prettyPrint = true } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Layouts", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Inspector для widget layouts и grid states с реальными preset-идентификаторами из web-клиента.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Widget Layouts", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("dashboard", "object-main", "object-documents", "object-ref-tables", "admin-widgets").forEach {
                            OutlinedButton(onClick = { onLayoutPreset(it) }) { Text(it) }
                        }
                    }
                    OutlinedTextField(value = state.pageType, onValueChange = onUpdatePageType, label = { Text("page_type") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = state.objectId, onValueChange = onUpdateObjectId, label = { Text("object_id") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = state.typeId, onValueChange = onUpdateTypeId, label = { Text("type_id") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = onLoadLayouts) { Text("Загрузить layouts") }
                }
            }
        }
        if (state.layouts.isEmpty() && !state.isLoading) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text("Layouts не найдены", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.layouts, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.pageType} • ${item.scope}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            listOfNotNull(item.objectId, item.typeId).joinToString(" • ").ifBlank { "global match" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(item.layout?.let(json::encodeToString).orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Grid State", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("documents", "admin-users", "admin-object-types", "admin-requisites", "admin-ref-tables").forEach {
                            OutlinedButton(onClick = { onGridPreset(it) }) { Text(it) }
                        }
                    }
                    OutlinedTextField(value = state.gridId, onValueChange = onUpdateGridId, label = { Text("grid_id") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = onLoadGridState) { Text("Загрузить grid state") }
                    state.gridState?.let { gs ->
                        Text("Scope: ${gs.scope ?: "none"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(gs.state?.let(json::encodeToString).orEmpty().ifBlank { "null" }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
