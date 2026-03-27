package ru.custle.mobile.feature.superadmin

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
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.data.SuperadminBundle
import ru.custle.mobile.core.model.SuperadminStatsDto
import ru.custle.mobile.core.model.SuperadminUserDto
import ru.custle.mobile.core.model.SuperadminWorkspaceDto

data class SuperadminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stats: SuperadminStatsDto = SuperadminStatsDto(),
    val workspaces: List<SuperadminWorkspaceDto> = emptyList(),
    val users: List<SuperadminUserDto> = emptyList(),
    val settingsJson: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

class SuperadminViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(SuperadminUiState())
    val state: StateFlow<SuperadminUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val bundle: SuperadminBundle = container.superadminRepository.bootstrap()
            _state.value = _state.value.copy(
                isLoading = false,
                stats = bundle.stats,
                workspaces = bundle.workspaces,
                users = bundle.users,
                settingsJson = bundle.settings,
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

class SuperadminViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SuperadminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SuperadminViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun SuperadminRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { SuperadminViewModelFactory(container) }
    val viewModel: SuperadminViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    SuperadminScreen(state = state, onRefresh = viewModel::refresh)
}

@Composable
fun SuperadminScreen(
    state: SuperadminUiState,
    onRefresh: () -> Unit,
) {
    var tab by remember { mutableStateOf("stats") }
    val json = remember { Json { prettyPrint = true } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Superadmin", style = MaterialTheme.typography.headlineSmall)
            Text("Read-only inspector для platform stats, workspaces, users и global settings.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh) { Text("Обновить") }
                OutlinedButton(onClick = { tab = "stats" }) { Text("Stats") }
                OutlinedButton(onClick = { tab = "workspaces" }) { Text("Workspaces") }
                OutlinedButton(onClick = { tab = "users" }) { Text("Users") }
                OutlinedButton(onClick = { tab = "settings" }) { Text("Settings") }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
        when (tab) {
            "stats" -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Platform Stats", style = MaterialTheme.typography.titleMedium)
                            Text("Workspaces: ${state.stats.totalWorkspaces} total / ${state.stats.activeWorkspaces} active", style = MaterialTheme.typography.bodySmall)
                            Text("Users: ${state.stats.totalUsers} total / ${state.stats.newUsers7d} new 7d", style = MaterialTheme.typography.bodySmall)
                            Text("Objects: ${state.stats.totalObjects}", style = MaterialTheme.typography.bodySmall)
                            Text("Ref tables: ${state.stats.totalRefTables}, ref records: ${state.stats.totalRefRecords}", style = MaterialTheme.typography.bodySmall)
                            Text("New workspaces 7d: ${state.stats.newWorkspaces7d}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            "workspaces" -> {
                items(state.workspaces, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOfNotNull(
                                    item.slug,
                                    item.ownerEmail,
                                    if (item.isSystem) "system" else null,
                                    if (item.isActive) "active" else "inactive",
                                ).joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text("members=${item.memberCount} • objects=${item.objectCount} • docs=${item.docCount}", style = MaterialTheme.typography.bodySmall)
                            Text("doc bytes=${item.docSizeBytes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            "users" -> {
                items(state.users, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(listOf(item.firstName, item.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { item.email }, style = MaterialTheme.typography.titleMedium)
                            Text(
                                listOfNotNull(
                                    item.email,
                                    if (item.isSuperAdmin) "superadmin" else null,
                                    if (item.isActive) "active" else "inactive",
                                    "workspaces=${item.workspaceCount}",
                                ).joinToString(" • "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
            else -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Global Settings", style = MaterialTheme.typography.titleMedium)
                            Text(json.encodeToString(state.settingsJson), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
