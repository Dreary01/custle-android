@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppUpdateCheckResult
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.TelegramStatusDto
import ru.custle.mobile.core.model.UserDto
import ru.custle.mobile.core.model.WorkspaceMemberDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner

@Composable
fun ProfileScreen(
    user: UserDto?,
    telegramStatus: TelegramStatusDto?,
    telegramCode: String,
    workspaceMembers: List<WorkspaceMemberDto>,
    workspaceInviteToken: String,
    onSave: (String, String, String) -> Unit,
    onInviteWorkspaceMember: (String, String) -> Unit,
    onUpdateWorkspaceMemberRole: (String, String) -> Unit,
    onRemoveWorkspaceMember: (String) -> Unit,
    onGenerateTelegramCode: () -> Unit,
    onUpdateTelegramAutoDelete: (Int) -> Unit,
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable(user?.id, user?.firstName) { mutableStateOf(user?.firstName.orEmpty()) }
    var lastName by rememberSaveable(user?.id, user?.lastName) { mutableStateOf(user?.lastName.orEmpty()) }
    var email by rememberSaveable(user?.id, user?.email) { mutableStateOf(user?.email.orEmpty()) }
    var inviteEmail by rememberSaveable { mutableStateOf("") }
    var inviteRole by rememberSaveable { mutableStateOf("member") }
    var updateInfo by remember { mutableStateOf<AppUpdateCheckResult?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var installingUpdate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        checkingUpdate = true
        runCatching { container.appUpdateRepository.check() }
            .onSuccess {
                updateInfo = it
                updateError = null
            }
            .onFailure {
                updateError = it.message
            }
        checkingUpdate = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ProfileHero(user = user, membersCount = workspaceMembers.size)
        }
        item {
            ThemeCard()
        }
        item {
            UpdateCard(
                updateInfo = updateInfo,
                updateError = updateError,
                checkingUpdate = checkingUpdate,
                installingUpdate = installingUpdate,
                installedVersionName = container.appUpdateRepository.installedVersionName(),
                canRequestPackageInstalls = container.appUpdateRepository.canRequestPackageInstalls(),
                onCheck = {
                    scope.launch {
                        checkingUpdate = true
                        runCatching { container.appUpdateRepository.check() }
                            .onSuccess {
                                updateInfo = it
                                updateError = null
                            }
                            .onFailure {
                                updateError = it.message
                            }
                        checkingUpdate = false
                    }
                },
                onInstall = {
                    scope.launch {
                        installingUpdate = true
                        updateError = null
                        runCatching {
                            val apk = container.appUpdateRepository.downloadLatestApk(updateInfo!!.latest.apkUrl)
                            val intent = container.appUpdateRepository.buildInstallIntent(apk)
                            context.startActivity(intent)
                        }.onFailure {
                            updateError = it.message ?: "Не удалось установить обновление"
                        }
                        installingUpdate = false
                    }
                },
            )
        }
        item {
            ProfileFormCard(
                firstName = firstName,
                lastName = lastName,
                email = email,
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                onEmailChange = { email = it },
                onSave = { onSave(firstName.trim(), lastName.trim(), email.trim()) },
            )
        }
        item {
            TelegramCard(
                telegramStatus = telegramStatus,
                telegramCode = telegramCode,
                onGenerateTelegramCode = onGenerateTelegramCode,
                onUpdateTelegramAutoDelete = onUpdateTelegramAutoDelete,
            )
        }
        if (user?.isAdmin == true) {
            item {
                InviteCard(
                    inviteEmail = inviteEmail,
                    inviteRole = inviteRole,
                    workspaceInviteToken = workspaceInviteToken,
                    onInviteEmailChange = { inviteEmail = it },
                    onInviteRoleChange = { inviteRole = it },
                    onCreateInvite = {
                        onInviteWorkspaceMember(inviteEmail.trim(), inviteRole)
                        inviteEmail = ""
                    },
                )
            }
        }
        item {
            Text(
                "Участники workspace",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (workspaceMembers.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Участников нет",
                    message = "Список участников workspace появится здесь.",
                )
            }
        } else {
            items(workspaceMembers, key = { it.id }) { member ->
                MemberCard(
                    member = member,
                    currentUser = user,
                    onUpdateWorkspaceMemberRole = onUpdateWorkspaceMemberRole,
                    onRemoveWorkspaceMember = onRemoveWorkspaceMember,
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(
    user: UserDto?,
    membersCount: Int,
) {
    val displayName = listOfNotNull(user?.firstName, user?.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { user?.email ?: "Профиль" }

    AppHeroCard(
        title = displayName,
        subtitle = user?.email ?: "",
        chips = listOf(
            (user?.email ?: "Email не указан") to Icons.Outlined.Verified,
            "$membersCount участников" to Icons.Outlined.Groups,
        ),
    )
}

@Composable
private fun UpdateCard(
    updateInfo: AppUpdateCheckResult?,
    updateError: String?,
    checkingUpdate: Boolean,
    installingUpdate: Boolean,
    installedVersionName: String,
    canRequestPackageInstalls: Boolean,
    onCheck: () -> Unit,
    onInstall: () -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Обновление", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "Установлено: ${updateInfo?.currentVersionName ?: installedVersionName}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                checkingUpdate -> Text("Проверка...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                updateError != null -> ErrorBanner(updateError)
                updateInfo?.updateAvailable == true -> {
                    Text("Доступно: ${updateInfo.latest.versionName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onInstall,
                            enabled = !installingUpdate,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(if (installingUpdate) "Скачивание..." else "Обновить")
                        }
                        TextButton(
                            onClick = onCheck,
                            enabled = !checkingUpdate && !installingUpdate,
                        ) {
                            Text("Проверить")
                        }
                    }
                    if (!canRequestPackageInstalls) {
                        Text(
                            "Может потребоваться разрешение на установку.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    Text(
                        "Актуальная версия.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = onCheck,
                        enabled = !checkingUpdate && !installingUpdate,
                    ) {
                        Text("Проверить")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFormCard(
    firstName: String,
    lastName: String,
    email: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Личные данные", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Имя") },
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Фамилия") },
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
            )
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
private fun TelegramCard(
    telegramStatus: TelegramStatusDto?,
    telegramCode: String,
    onGenerateTelegramCode: () -> Unit,
    onUpdateTelegramAutoDelete: (Int) -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Telegram", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (telegramStatus?.linked == true) {
                ProfileMetaChip(
                    "@${telegramStatus.telegramUsername ?: telegramStatus.botUsername}",
                    Icons.AutoMirrored.Outlined.Send,
                )
                Text(
                    "Автоудаление: ${telegramStatus.autoDeleteMinutes ?: 0} мин",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0, 1, 5, 15, 30, 60, 1440).forEach { value ->
                        AssistChip(
                            onClick = { onUpdateTelegramAutoDelete(value) },
                            label = {
                                Text(if (value == 0) "Не удалять" else "$value мин")
                            },
                            leadingIcon = { Icon(Icons.Outlined.LockClock, contentDescription = null) },
                        )
                    }
                }
            } else {
                Text(
                    "Telegram не привязан.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (telegramCode.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            "Код: $telegramCode",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Button(onClick = onGenerateTelegramCode, modifier = Modifier.fillMaxWidth()) {
                    Text("Получить код привязки")
                }
            }
        }
    }
}

@Composable
private fun InviteCard(
    inviteEmail: String,
    inviteRole: String,
    workspaceInviteToken: String,
    onInviteEmailChange: (String) -> Unit,
    onInviteRoleChange: (String) -> Unit,
    onCreateInvite: () -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Пригласить участника", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            OutlinedTextField(
                value = inviteEmail,
                onValueChange = onInviteEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InviteRoleChip(
                    selected = inviteRole == "member",
                    label = "member",
                    onClick = { onInviteRoleChange("member") },
                )
                InviteRoleChip(
                    selected = inviteRole == "admin",
                    label = "admin",
                    onClick = { onInviteRoleChange("admin") },
                )
            }
            Button(
                onClick = onCreateInvite,
                enabled = inviteEmail.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Создать invite")
            }
            if (workspaceInviteToken.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "Token: $workspaceInviteToken",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: WorkspaceMemberDto,
    currentUser: UserDto?,
    onUpdateWorkspaceMemberRole: (String, String) -> Unit,
    onRemoveWorkspaceMember: (String) -> Unit,
) {
    DsCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                listOfNotNull(member.firstName, member.lastName).joinToString(" ").ifBlank { member.email },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                listOfNotNull(member.email, member.role, if (member.isActive) "active" else "inactive").joinToString(" / "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (currentUser?.isAdmin == true && member.id != currentUser.id) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InviteRoleChip(
                        selected = member.role == "member",
                        label = "member",
                        onClick = { onUpdateWorkspaceMemberRole(member.id, "member") },
                    )
                    InviteRoleChip(
                        selected = member.role == "admin",
                        label = "admin",
                        onClick = { onUpdateWorkspaceMemberRole(member.id, "admin") },
                    )
                    AssistChip(
                        onClick = { onRemoveWorkspaceMember(member.id) },
                        label = { Text("Удалить") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMetaChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Composable
private fun InviteRoleChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(if (selected) "[$label]" else label) },
        leadingIcon = {
            Icon(
                if (selected) Icons.Outlined.Verified else Icons.Outlined.InstallMobile,
                contentDescription = null,
            )
        },
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard() {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val isDark by container.sessionStore.darkThemeFlow.collectAsState(initial = true)

    DsCard {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Оформление", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = isDark,
                    onClick = {
                        scope.launch { container.sessionStore.saveDarkTheme(true) }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(isDark) {
                            Icon(
                                Icons.Outlined.DarkMode,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        }
                    },
                ) {
                    Text("Тёмная")
                }
                SegmentedButton(
                    selected = !isDark,
                    onClick = {
                        scope.launch { container.sessionStore.saveDarkTheme(false) }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        SegmentedButtonDefaults.Icon(!isDark) {
                            Icon(
                                Icons.Outlined.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        }
                    },
                ) {
                    Text("Светлая")
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
