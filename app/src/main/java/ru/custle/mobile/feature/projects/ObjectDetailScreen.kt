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
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.WorkspaceMemberDto

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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ObjectHeader(
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
        item { OverviewSection(bundle = bundle) }
        item { PlansSection(bundle = bundle) }
        item {
            ParticipantsSection(
                participants = bundle.participants,
                workspaceMembers = workspaceMembers,
                isSaving = isSavingParticipants,
                onAddParticipant = onAddParticipant,
                onUpdateParticipantRole = onUpdateParticipantRole,
                onRemoveParticipant = onRemoveParticipant,
            )
        }
        item { DependenciesSection(bundle = bundle) }
        if (bundle.detail.children.isNotEmpty()) {
            item {
                SectionCard(title = "Дочерние объекты") {
                    bundle.detail.children.forEach { child ->
                        ChildObjectRow(child = child, onOpenChildObject = onOpenChildObject)
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectHeader(
    bundle: ObjectDetailBundle,
    onBack: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenDiscussions: () -> Unit,
    onOpenTemplates: () -> Unit,
    onCreateTodo: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bundle.detail.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val typeName = bundle.detail.typeName ?: bundle.detail.typeId
                    Badge(typeName)
                    Badge(bundle.detail.status)
                }
            }
        }
        val crumbs = bundle.ancestors.joinToString(" / ") { it.name }
        if (crumbs.isNotBlank()) {
            Text(
                crumbs,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MetricChip("${bundle.detail.progress}%")
            bundle.detail.priority.takeIf { it > 0 }?.let { MetricChip("P$it") }
            MetricChip("${bundle.detail.children.size} дч.")
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ActionChip("Документы", Icons.Outlined.Description, onOpenDocuments)
            ActionChip("Обсуждения", Icons.Outlined.Forum, onOpenDiscussions)
            ActionChip("Генерация", Icons.Outlined.LibraryAddCheck, onOpenTemplates)
            ActionChip("Задача", Icons.Outlined.TaskAlt, onCreateTodo)
        }
    }
}

@Composable
private fun Badge(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun MetricChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OverviewSection(bundle: ObjectDetailBundle) {
    SectionCard(title = "Обзор") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Badge("Прогресс ${bundle.detail.progress}%")
            bundle.detail.priority.takeIf { it > 0 }?.let { Badge("Приоритет $it") }
            bundle.detail.ownerName?.takeIf { it.isNotBlank() }?.let { Badge(it) }
            bundle.detail.assigneeName?.takeIf { it.isNotBlank() }?.let { Badge(it) }
        }
        val facts = listOfNotNull(
            bundle.detail.actualStartDate?.let { "Факт старт: $it" },
            bundle.detail.actualEndDate?.let { "Факт завершение: $it" },
        )
        facts.forEach { fact ->
            Text(
                fact,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        bundle.detail.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Text(
                desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun PlansSection(bundle: ObjectDetailBundle) {
    SectionCard(title = "Планы") {
        val lines = bundle.plans.map {
            listOfNotNull(it.planType, it.startDate, it.endDate, it.durationDays?.let { d -> "$d дн" })
                .joinToString(" / ")
        }
        if (lines.isEmpty()) {
            Text(
                "Нет данных",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            lines.forEach { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DependenciesSection(bundle: ObjectDetailBundle) {
    SectionCard(title = "Зависимости") {
        val lines = bundle.dependencies.map {
            "${it.type}: ${it.predecessorId} -> ${it.successorId} (lag ${it.lagDays})"
        }
        if (lines.isEmpty()) {
            Text(
                "Нет зависимостей",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            lines.forEach { line ->
                Text(
                    line,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
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

    SectionCard(title = "Участники") {
        if (participants.isEmpty()) {
            Text(
                "Нет участников",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.PersonAddAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Добавить участника",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    if (availableMembers.isEmpty()) {
                        Text(
                            "Все добавлены",
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
                            Text(if (isSaving) "..." else "Добавить")
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        participant.userName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
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
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            RolePicker(currentRole = role, onSelect = { role = it })
            if (role != participant.role) {
                Button(
                    onClick = { onUpdateParticipantRole(participant.userId, role) },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isSaving) "..." else "Сохранить роль")
                }
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Поиск") },
            singleLine = true,
        )
        if (selected != null) {
            Text(
                memberLabel(selected),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        filtered.take(5).forEach { member ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(member.id) },
                shape = RoundedCornerShape(8.dp),
                color = if (member.id == selectedUserId) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        memberLabel(member),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
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
    currentRole: String,
    onSelect: (String) -> Unit,
) {
    val options = listOf("member", "manager", "executor", "observer")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        options.forEach { option ->
            AssistChip(
                onClick = { onSelect(option) },
                label = {
                    Text(option.replaceFirstChar(Char::uppercase))
                },
                leadingIcon = if (currentRole == option) {
                    {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
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

@Composable
private fun ChildObjectRow(
    child: ru.custle.mobile.core.model.ObjectNodeDto,
    onOpenChildObject: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChildObject(child.id) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    child.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val meta = listOfNotNull(child.typeName, child.status).joinToString(" / ")
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
}

@Composable
private fun ActionChip(
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
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

private fun memberLabel(member: WorkspaceMemberDto): String =
    listOfNotNull(member.firstName, member.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { member.email }
