package ru.custle.mobile.feature.admin

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.custle.mobile.core.data.AdminBundle
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.AdminUserDto
import ru.custle.mobile.core.model.PermissionDto
import ru.custle.mobile.core.ui.components.ErrorBanner

data class AdminUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val users: List<AdminUserDto> = emptyList(),
    val selectedUser: AdminUserDto? = null,
    val permissions: List<PermissionDto> = emptyList(),
    val settingsJson: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
    val modulesJson: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
)

class AdminViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val bundle: AdminBundle = container.adminRepository.bootstrap()
            val selectedId = _state.value.selectedUser?.id
            val selected = bundle.users.firstOrNull { it.id == selectedId }
            val permissions = selected?.let { runCatching { container.adminRepository.permissionsByUser(it.id) }.getOrDefault(emptyList()) }
                ?: emptyList()
            _state.value = _state.value.copy(
                isLoading = false,
                users = bundle.users,
                selectedUser = selected,
                permissions = permissions,
                settingsJson = bundle.settings,
                modulesJson = bundle.modules,
            )
        }
    }

    fun openUser(id: String) {
        val user = _state.value.users.firstOrNull { it.id == id } ?: return
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val permissions = container.adminRepository.permissionsByUser(id)
            _state.value = _state.value.copy(
                isLoading = false,
                selectedUser = user,
                permissions = permissions,
            )
        }
    }

    fun closeUser() {
        _state.value = _state.value.copy(selectedUser = null, permissions = emptyList())
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

class AdminViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun AdminRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { AdminViewModelFactory(container) }
    val viewModel: AdminViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    AdminScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenUser = viewModel::openUser,
        onCloseUser = viewModel::closeUser,
    )
}

@Composable
fun AdminScreen(
    state: AdminUiState,
    onRefresh: () -> Unit,
    onOpenUser: (String) -> Unit,
    onCloseUser: () -> Unit,
) {
    var tab by remember { mutableStateOf("users") }
    val json = remember { Json { prettyPrint = true } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Администрирование",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefresh) { Text("Обновить") }
                OutlinedButton(onClick = { tab = "users" }) { Text("Пользователи") }
                OutlinedButton(onClick = { tab = "settings" }) { Text("Настройки") }
                OutlinedButton(onClick = { tab = "modules" }) { Text("Модули") }
            }
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        if (tab == "users" && state.selectedUser != null) {
            item {
                DsCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            listOfNotNull(state.selectedUser.firstName, state.selectedUser.lastName).joinToString(" ").ifBlank { state.selectedUser.email },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            listOfNotNull(
                                state.selectedUser.email,
                                if (state.selectedUser.isAdmin) "admin" else null,
                                if (state.selectedUser.isActive) "active" else "inactive",
                            ).joinToString(" / "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("${state.permissions.size} permissions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        state.permissions.forEach { perm ->
                            Text(
                                listOfNotNull(
                                    perm.resourceType,
                                    perm.resourceName ?: perm.resourceId,
                                    "actions=${perm.actions}",
                                    if (perm.recursive) "recursive" else null,
                                ).joinToString(" / "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedButton(onClick = onCloseUser) { Text("Свернуть") }
                    }
                }
            }
        }
        when (tab) {
            "users" -> {
                if (state.users.isEmpty() && !state.isLoading) {
                    item { EmptyCard("Пользователей нет") }
                } else {
                    items(state.users, key = { it.id }) { item ->
                        DsCard(modifier = Modifier.clickable { onOpenUser(item.id) }) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    listOfNotNull(item.firstName, item.lastName).joinToString(" ").ifBlank { item.email },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    listOfNotNull(item.email, if (item.isAdmin) "admin" else null, if (item.isActive) "active" else "inactive").joinToString(" / "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            "settings" -> {
                item {
                    DsCard {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Настройки workspace", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(json.encodeToString(state.settingsJson), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            else -> {
                item {
                    DsCard {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Модули", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(json.encodeToString(state.modulesJson), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun EmptyCard(text: String) {
    DsCard {
        Text(text, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
