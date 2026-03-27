@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.workspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.WorkspaceDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun WorkspacePickerScreen(
    workspaces: List<WorkspaceDto>,
    isBusy: Boolean,
    errorMessage: String?,
    onSelect: (String) -> Unit,
    onAcceptInvitation: (String) -> Unit,
) {
    var inviteToken by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AppHeroCard(
                title = "Выбор workspace",
                subtitle = "После входа нужно активировать рабочее пространство. Здесь пользователь должен быстро понять, куда он попадёт дальше, и при необходимости принять приглашение.",
                chips = listOf(
                    "${workspaces.size} workspace" to Icons.Outlined.Workspaces,
                    "Invite token" to Icons.Outlined.Key,
                ),
            )
        }
        item {
            AppSectionCard(
                title = "Принять приглашение",
                hint = "Если нужного workspace нет в списке, можно зайти по invite token.",
            ) {
                OutlinedTextField(
                    value = inviteToken,
                    onValueChange = { inviteToken = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Invite token") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        onAcceptInvitation(inviteToken)
                        inviteToken = ""
                    },
                    enabled = !isBusy && inviteToken.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Принять приглашение")
                }
            }
        }
        errorMessage?.takeIf { it.isNotBlank() }?.let {
            item {
                ErrorBanner(it)
            }
        }
        if (workspaces.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Доступных workspace пока нет",
                    message = "Можно принять приглашение по токену выше. Когда список появится, здесь должен быть простой выбор без лишней служебной терминологии.",
                )
            }
        } else {
            items(workspaces, key = { it.id }) { workspace ->
                WorkspaceRow(
                    workspace = workspace,
                    isBusy = isBusy,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun WorkspaceRow(
    workspace: WorkspaceDto,
    isBusy: Boolean,
    onSelect: (String) -> Unit,
) {
    AppSectionCard(
        title = workspace.name,
        hint = "Роль: ${workspace.role ?: "member"}",
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
            ) {
                Text(
                    text = workspace.role ?: "member",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        Button(
            onClick = { onSelect(workspace.id) },
            enabled = !isBusy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.material3.Icon(Icons.Outlined.Groups, contentDescription = null)
            Text("Открыть workspace")
        }
    }
}
