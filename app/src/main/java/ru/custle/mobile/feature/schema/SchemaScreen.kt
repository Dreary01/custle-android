package ru.custle.mobile.feature.schema

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ru.custle.mobile.core.model.ObjectTypeDto
import ru.custle.mobile.core.model.RequisiteDto
import ru.custle.mobile.core.model.RequisiteGroupDto
import ru.custle.mobile.core.ui.components.ErrorBanner

data class SchemaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val objectTypes: List<ObjectTypeDto> = emptyList(),
    val requisites: List<RequisiteDto> = emptyList(),
    val groups: List<RequisiteGroupDto> = emptyList(),
    val selectedObjectType: ObjectTypeDto? = null,
)

class SchemaViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(SchemaUiState())
    val state: StateFlow<SchemaUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val bundle = container.schemaRepository.load()
            val selectedId = _state.value.selectedObjectType?.id
            val selected = selectedId?.let { runCatching { container.schemaRepository.objectType(it) }.getOrNull() }
            _state.value = _state.value.copy(
                isLoading = false,
                objectTypes = bundle.objectTypes,
                requisites = bundle.requisites,
                groups = bundle.groups,
                selectedObjectType = selected,
            )
        }
    }

    fun openObjectType(id: String) {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val detail = container.schemaRepository.objectType(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selectedObjectType = detail,
            )
        }
    }

    fun closeObjectType() {
        _state.value = _state.value.copy(selectedObjectType = null)
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

class SchemaViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchemaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchemaViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun SchemaRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { SchemaViewModelFactory(container) }
    val viewModel: SchemaViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    SchemaScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenObjectType = viewModel::openObjectType,
        onCloseObjectType = viewModel::closeObjectType,
    )
}

@Composable
fun SchemaScreen(
    state: SchemaUiState,
    onRefresh: () -> Unit,
    onOpenObjectType: (String) -> Unit,
    onCloseObjectType: () -> Unit,
) {
    var tab by remember { mutableStateOf("types") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Схема",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh) { Text("Обновить") }
                OutlinedButton(onClick = { tab = "types" }) { Text("Типы") }
                OutlinedButton(onClick = { tab = "reqs" }) { Text("Реквизиты") }
                OutlinedButton(onClick = { tab = "groups" }) { Text("Группы") }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        state.selectedObjectType?.let { item ->
            item {
                ObjectTypeDetailCard(item = item, onClose = onCloseObjectType)
            }
        }
        when (tab) {
            "types" -> {
                if (state.objectTypes.isEmpty() && !state.isLoading) {
                    item { EmptyCard("Типов объектов нет") }
                } else {
                    items(state.objectTypes, key = { it.id }) { item ->
                        ObjectTypeRow(item = item, isSelected = state.selectedObjectType?.id == item.id, onOpen = onOpenObjectType)
                    }
                }
            }
            "reqs" -> {
                if (state.requisites.isEmpty() && !state.isLoading) {
                    item { EmptyCard("Реквизитов нет") }
                } else {
                    items(state.requisites, key = { it.id }) { item ->
                        RequisiteRow(item = item)
                    }
                }
            }
            else -> {
                if (state.groups.isEmpty() && !state.isLoading) {
                    item { EmptyCard("Групп нет") }
                } else {
                    items(state.groups, key = { it.id }) { item ->
                        GroupRow(item = item)
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

@Composable
private fun EmptyCard(text: String) {
    DsCard {
        Text(text, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ObjectTypeDetailCard(
    item: ObjectTypeDto,
    onClose: () -> Unit,
) {
    DsCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            val meta = listOfNotNull(
                item.kind.takeIf { it.isNotBlank() },
                item.color,
                if (item.canBeRoot) "root" else null,
                if (item.addToCalendar) "calendar" else null,
                if (item.checkUniqueness) "unique-check" else null,
            ).joinToString(" / ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${item.requisites.size} реквизитов", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            item.requisites.forEach { req ->
                val line = listOfNotNull(
                    req.requisite?.name ?: req.requisiteId,
                    req.requisite?.type,
                    if (req.isRequired) "required" else null,
                    if (req.isVisible) "visible" else "hidden",
                    if (req.inheritToChildren) "inherit" else null,
                ).joinToString(" / ")
                Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.childTypeIds.isNotEmpty()) {
                Text("Child types: ${item.childTypeIds.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.refTableIds.isNotEmpty()) {
                Text("Ref tables: ${item.refTableIds.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onClose) { Text("Свернуть") }
        }
    }
}

@Composable
private fun ObjectTypeRow(
    item: ObjectTypeDto,
    isSelected: Boolean,
    onOpen: (String) -> Unit,
) {
    DsCard(modifier = Modifier.clickable { onOpen(item.id) }) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            val meta = listOfNotNull(
                item.kind.takeIf { it.isNotBlank() },
                item.color,
                if (item.canBeRoot) "root" else null,
            ).joinToString(" / ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(it.take(160) + if (it.length > 160) "..." else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Text("Выбрано", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun RequisiteRow(item: RequisiteDto) {
    DsCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            val meta = listOfNotNull(item.type.takeIf { it.isNotBlank() }, if (item.isUnique) "unique" else null).joinToString(" / ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GroupRow(item: RequisiteGroupDto) {
    DsCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("sort ${item.sortOrder}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
