package ru.custle.mobile.feature.marketplace

import androidx.compose.foundation.clickable
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.MarketplaceConfigDto
import ru.custle.mobile.core.model.MarketplaceInstallationDto

data class MarketplaceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val configs: List<MarketplaceConfigDto> = emptyList(),
    val installations: List<MarketplaceInstallationDto> = emptyList(),
    val selectedInstallation: MarketplaceInstallationDto? = null,
    val syncStatus: JsonElement? = null,
)

class MarketplaceViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(MarketplaceUiState())
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val bundle = container.marketplaceRepository.bootstrap()
            _state.value = _state.value.copy(
                isLoading = false,
                configs = bundle.configs,
                installations = bundle.installations,
                selectedInstallation = bundle.installations.firstOrNull { it.id == _state.value.selectedInstallation?.id },
            )
        }
    }

    fun openInstallation(id: String) {
        val installation = _state.value.installations.firstOrNull { it.id == id } ?: return
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val syncStatus = container.marketplaceRepository.syncStatus(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selectedInstallation = installation,
                syncStatus = syncStatus,
            )
        }
    }

    fun closeInstallation() {
        _state.value = _state.value.copy(selectedInstallation = null, syncStatus = null)
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

class MarketplaceViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun MarketplaceRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { MarketplaceViewModelFactory(container) }
    val viewModel: MarketplaceViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    MarketplaceScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenInstallation = viewModel::openInstallation,
        onCloseInstallation = viewModel::closeInstallation,
    )
}

@Composable
fun MarketplaceScreen(
    state: MarketplaceUiState,
    onRefresh: () -> Unit,
    onOpenInstallation: (String) -> Unit,
    onCloseInstallation: () -> Unit,
) {
    var tab by remember { mutableStateOf("configs") }
    val json = remember { Json { prettyPrint = true } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Marketplace", style = MaterialTheme.typography.headlineSmall)
            Text("Read-only/admin-light inspector для marketplace configs и installations.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh) { Text("Обновить") }
                OutlinedButton(onClick = { tab = "configs" }) { Text("Configs") }
                OutlinedButton(onClick = { tab = "installs" }) { Text("Installations") }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
        if (tab == "installs" && state.selectedInstallation != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.selectedInstallation.configName, style = MaterialTheme.typography.titleLarge)
                        Text(state.selectedInstallation.installedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Credentials", style = MaterialTheme.typography.titleSmall)
                        Text(state.selectedInstallation.credentials?.let(json::encodeToString).orEmpty(), style = MaterialTheme.typography.bodySmall)
                        Text("Sync status", style = MaterialTheme.typography.titleSmall)
                        Text(state.syncStatus?.let(json::encodeToString).orEmpty().ifBlank { "null" }, style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onCloseInstallation) { Text("Свернуть") }
                    }
                }
            }
        }
        if (tab == "configs") {
            if (state.configs.isEmpty() && !state.isLoading) {
                item { EmptyCard("Marketplace configs не найдены") }
            } else {
                items(state.configs, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOfNotNull(item.category, if (item.isPublished) "published" else "draft", if (item.installed) "installed" else null).joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            item.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        } else {
            if (state.installations.isEmpty() && !state.isLoading) {
                item { EmptyCard("Установок нет") }
            } else {
                items(state.installations, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenInstallation(item.id) }) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.configName, style = MaterialTheme.typography.titleMedium)
                            Text(item.installedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(text, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
