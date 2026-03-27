package ru.custle.mobile.feature.reftables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import ru.custle.mobile.core.data.RefTableBundle
import ru.custle.mobile.core.model.RefRecordDto
import ru.custle.mobile.core.model.RefTableDto

data class RefTablesUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<RefTableDto> = emptyList(),
    val selected: RefTableBundle? = null,
)

class RefTablesViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(RefTablesUiState())
    val state: StateFlow<RefTablesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.refTableRepository.list()
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
                selected = _state.value.selected?.let { current ->
                    items.firstOrNull { it.id == current.table.id }?.let { updated ->
                        current.copy(table = updated)
                    }
                },
            )
        }
    }

    fun open(id: String) {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val detail = container.refTableRepository.detail(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selected = detail,
            )
        }
    }

    fun close() {
        _state.value = _state.value.copy(selected = null)
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

class RefTablesViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RefTablesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RefTablesViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun RefTablesRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { RefTablesViewModelFactory(container) }
    val viewModel: RefTablesViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    RefTablesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpen = viewModel::open,
        onClose = viewModel::close,
    )
}

@Composable
fun RefTablesScreen(
    state: RefTablesUiState,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
    onClose: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Справочники", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Read-only просмотр ref tables, колонок и записей. Конфигурирование и редактирование пока не перенесены.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Button(onClick = onRefresh) { Text("Обновить") }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        state.selected?.let { bundle ->
            item {
                RefTableDetailCard(bundle = bundle, onClose = onClose)
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Справочников пока нет",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                RefTableRow(item = item, isSelected = state.selected?.table?.id == item.id, onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun RefTableDetailCard(
    bundle: RefTableBundle,
    onClose: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(bundle.table.name, style = MaterialTheme.typography.titleLarge)
            val meta = listOfNotNull(
                bundle.table.objectName,
                bundle.table.structure.takeIf { it.isNotBlank() },
                bundle.table.inputMode.takeIf { it.isNotBlank() },
                if (bundle.table.hasApproval) "approval" else null,
                if (bundle.table.useDate) "date" else null,
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            bundle.table.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Text("Колонки: ${bundle.table.columns.size}", style = MaterialTheme.typography.titleSmall)
            bundle.table.columns.forEach { column ->
                val line = listOfNotNull(
                    column.requisite?.name ?: column.requisiteId,
                    column.requisite?.type?.takeIf { it.isNotBlank() },
                    column.aggregation?.takeIf { it.isNotBlank() },
                    if (column.isVisible) "visible" else "hidden",
                ).joinToString(" • ")
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("Записи: ${bundle.records.size}", style = MaterialTheme.typography.titleSmall)
            bundle.records.take(5).forEach { record ->
                RefRecordPreview(record = record)
            }
            if (bundle.records.size > 5) {
                Text(
                    "Показаны первые 5 записей",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onClose) { Text("Свернуть") }
        }
    }
}

@Composable
private fun RefRecordPreview(record: RefRecordDto) {
    val json = remember(record.id) { Json { prettyPrint = true } }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                listOfNotNull(record.recordDate, if (record.isApproved) "approved" else null).joinToString(" • "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                record.data?.let(json::encodeToString).orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RefTableRow(
    item: RefTableDto,
    isSelected: Boolean,
    onOpen: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(item.id) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(item.name, style = MaterialTheme.typography.titleMedium)
            val meta = listOfNotNull(
                item.objectName,
                item.structure.takeIf { it.isNotBlank() },
                item.inputMode.takeIf { it.isNotBlank() },
                if (item.isSystem) "system" else null,
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it.take(160) + if (it.length > 160) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (isSelected) {
                Text(
                    "Открыто выше",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
