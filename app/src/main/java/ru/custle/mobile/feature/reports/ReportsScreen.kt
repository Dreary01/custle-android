@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
import ru.custle.mobile.core.model.ReportDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

private val reportsJson = Json { prettyPrint = true }

data class ReportsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<ReportDto> = emptyList(),
    val selectedReport: ReportDto? = null,
)

class ReportsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.reportRepository.list()
            val selectedId = _state.value.selectedReport?.id
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
                selectedReport = items.firstOrNull { it.id == selectedId } ?: items.firstOrNull(),
            )
        }
    }

    fun open(id: String) {
        _state.value = _state.value.copy(selectedReport = _state.value.items.firstOrNull { it.id == id })
    }

    fun close() {
        _state.value = _state.value.copy(selectedReport = null)
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

class ReportsViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun ReportsRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { ReportsViewModelFactory(container) }
    val viewModel: ReportsViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    ReportsScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpen = viewModel::open,
        onClose = viewModel::close,
    )
}

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
    onClose: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            AppHeroCard(
                title = "Отчёты",
                subtitle = "${state.items.size} сохранённых отчётов",
                chips = listOf(
                    "${state.items.size} отчётов" to Icons.Outlined.Assessment,
                ),
            ) {
                Button(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Text("Обновить")
                }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        state.selectedReport?.let { report ->
            item {
                ReportDetailCard(report = report, onClose = onClose)
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyStateCard(
                    title = "Отчётов нет",
                    message = "Сохранённые отчёты появятся здесь.",
                )
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                ReportRow(item = item, isSelected = state.selectedReport?.id == item.id, onOpen = onOpen)
            }
        }
    }
}

@Composable
private fun ReportDetailCard(
    report: ReportDto,
    onClose: () -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(report.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                listOfNotNull(
                    "sort ${report.sortOrder}",
                    report.updatedAt.takeIf { it.isNotBlank() },
                    report.createdAt.takeIf { it.isNotBlank() },
                ).joinToString(" / "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            report.config?.let { config ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Config", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = reportsJson.encodeToString(config),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            OutlinedButton(onClick = onClose) {
                Text("Свернуть")
            }
        }
    }
}

@Composable
private fun ReportRow(
    item: ReportDto,
    isSelected: Boolean,
    onOpen: (String) -> Unit,
) {
    DsCard(modifier = Modifier.clickable { onOpen(item.id) }) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MetaPill("sort ${item.sortOrder}")
                item.updatedAt.takeIf { it.isNotBlank() }?.let { MetaPill(it) }
                if (isSelected) MetaPill("Выбрано")
            }
        }
    }
}

@Composable
private fun MetaPill(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
