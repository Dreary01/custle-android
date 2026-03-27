@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.WorkspaceMemberDto
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.theme.Brick300
import ru.custle.mobile.core.ui.theme.Brick600
import ru.custle.mobile.core.ui.theme.Green900
import ru.custle.mobile.core.ui.theme.Sand050
import ru.custle.mobile.core.ui.theme.Sand100
import ru.custle.mobile.core.ui.theme.Sand200
import ru.custle.mobile.core.ui.theme.Sand700

@Composable
fun ObjectDetailScreen(
    bundle: ObjectDetailBundle,
    workspaceMembers: List<WorkspaceMemberDto>,
    isSavingParticipants: Boolean,
    onOpenDocuments: () -> Unit,
    onOpenDiscussions: () -> Unit,
    onOpenTemplates: () -> Unit,
    onCreateTodo: (String, String?, String?) -> Unit,
    onAddParticipant: (String, String) -> Unit,
    onUpdateParticipantRole: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onOpenChildObject: (String) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ObjectHero(
                bundle = bundle,
                onBack = onBack,
                onOpenDocuments = onOpenDocuments,
                onOpenDiscussions = onOpenDiscussions,
                onOpenTemplates = onOpenTemplates,
                onCreateTodo = {
                    onCreateTodo("Задача: ${bundle.detail.name}", null, bundle.detail.id)
                },
            )
        }
        item { OverviewCard(bundle = bundle) }
        item {
            DetailPanel(
                marker = "PLANS",
                title = "Планы",
                hint = "Плановые интервалы и длительности объекта.",
                accent = Green900.copy(alpha = 0.10f),
            ) {
                DetailLines(
                    lines = bundle.plans.map {
                        listOfNotNull(it.planType, it.startDate, it.endDate, it.durationDays?.let { d -> "$d дн" })
                            .joinToString(" • ")
                    }.ifEmpty { listOf("Плановые данные пока не заполнены.") },
                )
            }
        }
        item {
            ParticipantsCard(
                participants = bundle.participants,
                workspaceMembers = workspaceMembers,
                isSaving = isSavingParticipants,
                onAddParticipant = onAddParticipant,
                onUpdateParticipantRole = onUpdateParticipantRole,
                onRemoveParticipant = onRemoveParticipant,
            )
        }
        item {
            DetailPanel(
                marker = "DEPENDENCIES",
                title = "Зависимости",
                hint = "Что блокирует или продолжает объект.",
                accent = Brick300.copy(alpha = 0.24f),
            ) {
                DetailLines(
                    lines = bundle.dependencies.map {
                        "${it.type}: ${it.predecessorId} -> ${it.successorId} (lag ${it.lagDays})"
                    }.ifEmpty { listOf("Зависимостей нет.") },
                )
            }
        }
        if (bundle.detail.children.isNotEmpty()) {
            item {
                DetailPanel(
                    marker = "CHILD OBJECTS",
                    title = "Дочерние объекты",
                    hint = "Продолжение структуры внутри текущего объекта.",
                    accent = Green900.copy(alpha = 0.08f),
                ) {
                    bundle.detail.children.forEach { child ->
                        ChildObjectCard(child = child, onOpenChildObject = onOpenChildObject)
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectHero(
    bundle: ObjectDetailBundle,
    onBack: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenDiscussions: () -> Unit,
    onOpenTemplates: () -> Unit,
    onCreateTodo: () -> Unit,
) {
    val crumbs = bundle.ancestors.joinToString(" / ") { it.name }
    val summary = listOfNotNull(
        bundle.detail.typeName ?: bundle.detail.typeId,
        bundle.detail.status,
        bundle.detail.assigneeName,
    ).joinToString(" • ")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Sand050),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Sand200.copy(alpha = 0.22f)),
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Green900,
                                    Green900.copy(alpha = 0.95f),
                                    Sand700,
                                ),
                            ),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Sand050.copy(alpha = 0.12f),
                    ) {
                        Text(
                            "OBJECT PROFILE",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Sand100,
                        )
                    }
                    Text(
                        bundle.detail.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Sand050,
                    )
                    val heroText = listOfNotNull(
                        crumbs.takeIf { it.isNotBlank() },
                        summary.takeIf { it.isNotBlank() },
                    ).joinToString("\n")
                    if (heroText.isNotBlank()) {
                        Text(
                            heroText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Sand200,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    value = "${bundle.detail.progress}%",
                    label = "Прогресс",
                    accent = Green900.copy(alpha = 0.12f),
                )
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    value = bundle.detail.priority.takeIf { it > 0 }?.toString() ?: "0",
                    label = "Приоритет",
                    accent = Brick300.copy(alpha = 0.45f),
                )
                DetailMetricCard(
                    modifier = Modifier.weight(1f),
                    value = bundle.detail.children.size.toString(),
                    label = "Дочерние",
                    accent = Sand200,
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    Text("К дереву")
                }
                ExecutiveActionChip("Документы", Icons.Outlined.Description, onOpenDocuments)
                ExecutiveActionChip("Обсуждения", Icons.Outlined.Forum, onOpenDiscussions)
                ExecutiveActionChip("Генерация", Icons.Outlined.LibraryAddCheck, onOpenTemplates)
                ExecutiveActionChip("Задача", Icons.Outlined.TaskAlt, onCreateTodo)
            }
        }
    }
}

@Composable
private fun DetailMetricCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = accent,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OverviewCard(
    bundle: ObjectDetailBundle,
) {
    DetailPanel(
        marker = "OVERVIEW",
        title = "Обзор объекта",
        hint = "Ключевые атрибуты без перегрузки вторичными действиями.",
        accent = Green900.copy(alpha = 0.10f),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KeyBadge("Прогресс ${bundle.detail.progress}%")
            bundle.detail.priority.takeIf { it > 0 }?.let { KeyBadge("Приоритет $it") }
            bundle.detail.ownerName?.takeIf { it.isNotBlank() }?.let { KeyBadge("Owner $it") }
            bundle.detail.assigneeName?.takeIf { it.isNotBlank() }?.let { KeyBadge("Исполнитель $it") }
        }
        FactsGrid(
            facts = listOfNotNull(
                bundle.detail.actualStartDate?.let { "Факт старт: $it" },
                bundle.detail.actualEndDate?.let { "Факт завершение: $it" },
            ),
        )
        bundle.detail.description?.takeIf { it.isNotBlank() }?.let { description ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Sand100,
            ) {
                Text(
                    text = description,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DetailLines(
    lines: List<String>,
) {
    lines.forEach { line ->
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Sand100,
        ) {
            Text(
                text = line,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ParticipantsCard(
    participants: List<ParticipantDto>,
    workspaceMembers: List<WorkspaceMemberDto>,
    isSaving: Boolean,
    onAddParticipant: (String, String) -> Unit,
    onUpdateParticipantRole: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
) {
    var selectedUserId by remember(participants, workspaceMembers) { mutableStateOf("") }
    var newRole by remember { mutableStateOf("member") }
    val existingIds = participants.map { it.userId }.toSet()
    val availableMembers = workspaceMembers.filter { it.id !in existingIds }

    DetailPanel(
        marker = "PARTICIPANTS",
        title = "Участники",
        hint = "Кто вовлечён в работу по объекту.",
        accent = Sand200.copy(alpha = 0.65f),
    ) {
        if (participants.isEmpty()) {
            EmptyInlineState("У объекта пока нет участников.")
        } else {
            participants.forEach { participant ->
                ParticipantRow(
                    participant = participant,
                    isSaving = isSaving,
                    onUpdateParticipantRole = onUpdateParticipantRole,
                    onRemoveParticipant = onRemoveParticipant,
                )
            }
        }
        if (workspaceMembers.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Sand100,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.PersonAddAlt, contentDescription = null, tint = Green900)
                        Text("Добавить участника", style = MaterialTheme.typography.titleSmall)
                    }
                    if (availableMembers.isEmpty()) {
                        Text(
                            "Все участники workspace уже добавлены.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        MemberPicker(
                            selectedUserId = selectedUserId,
                            members = availableMembers,
                            onSelect = { selectedUserId = it },
                        )
                        RolePicker(
                            label = "Роль в объекте",
                            currentRole = newRole,
                            onSelect = { newRole = it },
                        )
                        Button(
                            onClick = {
                                onAddParticipant(selectedUserId, newRole)
                                selectedUserId = ""
                                newRole = "member"
                            },
                            enabled = selectedUserId.isNotBlank() && !isSaving,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (isSaving) "Сохранение..." else "Добавить участника")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: ParticipantDto,
    isSaving: Boolean,
    onUpdateParticipantRole: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
) {
    var role by remember(participant.userId, participant.role) { mutableStateOf(participant.role) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Sand100,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Green900.copy(alpha = 0.08f),
            ) {
                Text(
                    "PARTICIPANT",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Green900,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(participant.userName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        participant.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { onRemoveParticipant(participant.userId) },
                    enabled = !isSaving,
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                }
            }
            RolePicker(
                label = "Роль",
                currentRole = role,
                onSelect = { role = it },
            )
            Button(
                onClick = { onUpdateParticipantRole(participant.userId, role) },
                enabled = role.isNotBlank() && role != participant.role && !isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSaving) "Сохранение..." else "Сохранить роль")
            }
        }
    }
}

@Composable
private fun MemberPicker(
    selectedUserId: String,
    members: List<WorkspaceMemberDto>,
    onSelect: (String) -> Unit,
) {
    val selected = members.firstOrNull { it.id == selectedUserId }
    var query by remember { mutableStateOf("") }
    val filtered = members.filter {
        query.isBlank() || it.email.contains(query, ignoreCase = true) ||
            listOfNotNull(it.firstName, it.lastName).joinToString(" ").contains(query, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск по участникам workspace") },
            singleLine = true,
        )
        if (selected != null) {
            Text(
                "Выбран: ${memberLabel(selected)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        filtered.take(6).forEach { member ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(member.id) },
                shape = RoundedCornerShape(18.dp),
                color = Sand050,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(memberLabel(member), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RolePicker(
    label: String,
    currentRole: String,
    onSelect: (String) -> Unit,
) {
    val options = listOf("member", "manager", "executor", "observer")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                AssistChip(
                    onClick = { onSelect(option) },
                    label = {
                        Text(if (currentRole == option) option.replaceFirstChar(Char::uppercase) else option)
                    },
                    leadingIcon = if (currentRole == option) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun ChildObjectCard(
    child: ru.custle.mobile.core.model.ObjectNodeDto,
    onOpenChildObject: (String) -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChildObject(child.id) },
        colors = CardDefaults.elevatedCardColors(containerColor = Sand050),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Green900.copy(alpha = 0.08f),
            ) {
                Text(
                    "CHILD OBJECT",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Green900,
                )
            }
            Text(
                child.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(child.typeName, child.status).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FactsGrid(
    facts: List<String>,
) {
    if (facts.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        facts.forEach { fact ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Sand050,
            ) {
                Text(
                    text = fact,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun KeyBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Sand200,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Green900,
        )
    }
}

@Composable
private fun ExecutiveActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}

@Composable
private fun EmptyInlineState(label: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Sand100,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailPanel(
    marker: String,
    title: String,
    hint: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Sand050,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.95f),
                            Sand050,
                        ),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Green900.copy(alpha = 0.10f),
                ) {
                    Text(
                        marker,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green900,
                    )
                }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

private fun memberLabel(member: WorkspaceMemberDto): String =
    listOfNotNull(member.firstName, member.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { member.email }
