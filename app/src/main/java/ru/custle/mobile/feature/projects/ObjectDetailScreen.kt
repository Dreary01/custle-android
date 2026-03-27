@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.WidgetPlacement
import ru.custle.mobile.core.model.WorkspaceMemberDto

// ── Accent tones ──
private val BlueDark = Color(0xFF1A2A4D)
private val BlueText = Color(0xFF8DB0F0)
private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)
private val AmberDark = Color(0xFF3D2E0A)
private val AmberText = Color(0xFFE8C060)

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
    val placements = bundle.widgetPlacements

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Header (always shown) ──
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

        // ── Portlets from layout ──
        items(placements, key = { it.widgetId }) { placement ->
            WidgetPortlet(
                placement = placement,
                bundle = bundle,
                workspaceMembers = workspaceMembers,
                isSavingParticipants = isSavingParticipants,
                onAddParticipant = onAddParticipant,
                onUpdateParticipantRole = onUpdateParticipantRole,
                onRemoveParticipant = onRemoveParticipant,
                onOpenChildObject = onOpenChildObject,
            )
        }

        // ── Participants (always at end if not in placements) ──
        if (placements.none { it.widgetId == "participants" }) {
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
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Portlet Router — renders the right widget for each placement
// ═══════════════════════════════════════════════════════════════

@Composable
private fun WidgetPortlet(
    placement: WidgetPlacement,
    bundle: ObjectDetailBundle,
    workspaceMembers: List<WorkspaceMemberDto>,
    isSavingParticipants: Boolean,
    onAddParticipant: (String, String) -> Unit,
    onUpdateParticipantRole: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
    onOpenChildObject: (String) -> Unit,
) {
    when (placement.widgetId) {
        "status-metrics" -> StatusMetricsPortlet(
            title = placement.title ?: "Статус",
            bundle = bundle,
        )
        "dates" -> DatesPortlet(
            title = placement.title ?: "Сроки",
            bundle = bundle,
        )
        "requisites" -> RequisitesPortlet(
            title = placement.title ?: "Реквизиты",
            bundle = bundle,
        )
        "description" -> DescriptionPortlet(
            title = placement.title ?: "Описание",
            bundle = bundle,
        )
        "hierarchy" -> HierarchyPortlet(
            title = placement.title ?: "Дочерние объекты",
            bundle = bundle,
            onOpenChildObject = onOpenChildObject,
        )
        "participants" -> ParticipantsSection(
            participants = bundle.participants,
            workspaceMembers = workspaceMembers,
            isSaving = isSavingParticipants,
            onAddParticipant = onAddParticipant,
            onUpdateParticipantRole = onUpdateParticipantRole,
            onRemoveParticipant = onRemoveParticipant,
        )
        "plans" -> PlansPortlet(
            title = placement.title ?: "Планы",
            bundle = bundle,
        )
        "dependencies" -> DependenciesPortlet(
            title = placement.title ?: "Зависимости",
            bundle = bundle,
        )
        // Unknown widget — skip silently
    }
}

// ═══════════════════════════════════════════════════════════════
// Status Metrics Portlet
// ═══════════════════════════════════════════════════════════════

@Composable
private fun StatusMetricsPortlet(title: String, bundle: ObjectDetailBundle) {
    PortletCard(title = title) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Progress
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${bundle.detail.progress}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldText,
                )
                LinearProgressIndicator(
                    progress = { bundle.detail.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = EmeraldText,
                    trackColor = EmeraldDark,
                    strokeCap = StrokeCap.Round,
                )
                Spacer(Modifier.height(4.dp))
                Text("Прогресс", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Status
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                StatusBadgeLarge(bundle.detail.status)
                Spacer(Modifier.height(4.dp))
                Text("Статус", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Priority
            if (bundle.detail.priority > 0) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "P${bundle.detail.priority}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (bundle.detail.priority) {
                            4 -> Color(0xFFE88080)
                            3 -> Color(0xFFE8A060)
                            2 -> AmberText
                            else -> BlueText
                        },
                    )
                    Text("Приоритет", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Dates Portlet
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DatesPortlet(title: String, bundle: ObjectDetailBundle) {
    val plans = bundle.plans
    val detail = bundle.detail

    PortletCard(title = title) {
        // Actual dates
        if (detail.actualStartDate != null || detail.actualEndDate != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                detail.actualStartDate?.let { date ->
                    DateItem(label = "Факт старт", value = date, modifier = Modifier.weight(1f))
                }
                detail.actualEndDate?.let { date ->
                    DateItem(label = "Факт завершение", value = date, modifier = Modifier.weight(1f))
                }
            }
        }

        // Plans
        plans.forEach { plan ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plan.planType?.replaceFirstChar { it.uppercase() } ?: "План",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val dates = listOfNotNull(plan.startDate, plan.endDate).joinToString(" → ")
                    if (dates.isNotBlank()) {
                        Text(dates, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                plan.durationDays?.let { d ->
                    Surface(shape = RoundedCornerShape(6.dp), color = BlueDark) {
                        Text(
                            "$d дн",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueText,
                        )
                    }
                }
            }
        }

        if (detail.actualStartDate == null && detail.actualEndDate == null && plans.isEmpty()) {
            Text("Нет данных о сроках", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DateItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ═══════════════════════════════════════════════════════════════
// Requisites Portlet (custom field values)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun RequisitesPortlet(title: String, bundle: ObjectDetailBundle) {
    val fieldValues = bundle.detail.fieldValues

    PortletCard(title = title) {
        if (fieldValues == null || fieldValues !is JsonObject || fieldValues.jsonObject.isEmpty()) {
            Text("Нет заполненных полей", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val obj = fieldValues.jsonObject
            obj.entries.forEachIndexed { index, (key, value) ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        key,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(0.4f),
                    )
                    Text(
                        value.jsonPrimitive.content,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Description Portlet
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DescriptionPortlet(title: String, bundle: ObjectDetailBundle) {
    val desc = bundle.detail.description
    PortletCard(title = title) {
        if (desc.isNullOrBlank()) {
            Text("Нет описания", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Hierarchy Portlet (children)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun HierarchyPortlet(title: String, bundle: ObjectDetailBundle, onOpenChildObject: (String) -> Unit) {
    val children = bundle.detail.children
    PortletCard(title = "$title (${children.size})") {
        if (children.isEmpty()) {
            Text("Нет дочерних объектов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            children.take(20).forEachIndexed { index, child ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChildObject(child.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            child.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val meta = listOfNotNull(child.typeName, child.status, child.assigneeName).joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (child.progress > 0) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldDark) {
                            Text(
                                "${child.progress}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldText,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (children.size > 20) {
                Text(
                    "Ещё ${children.size - 20} объектов...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Plans Portlet
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PlansPortlet(title: String, bundle: ObjectDetailBundle) {
    PortletCard(title = title) {
        if (bundle.plans.isEmpty()) {
            Text("Нет планов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            bundle.plans.forEachIndexed { index, plan ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            plan.planType?.replaceFirstChar { it.uppercase() } ?: "План",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        val dates = listOfNotNull(plan.startDate, plan.endDate).joinToString(" → ")
                        if (dates.isNotBlank()) {
                            Text(dates, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    plan.durationDays?.let { d ->
                        Surface(shape = RoundedCornerShape(6.dp), color = BlueDark) {
                            Text("$d дн", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = BlueText)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Dependencies Portlet
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DependenciesPortlet(title: String, bundle: ObjectDetailBundle) {
    PortletCard(title = title) {
        if (bundle.dependencies.isEmpty()) {
            Text("Нет зависимостей", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            bundle.dependencies.forEachIndexed { index, dep ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${dep.type}: ${dep.predecessorId.take(8)}→${dep.successorId.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (dep.lagDays != 0) {
                        Surface(shape = RoundedCornerShape(6.dp), color = AmberDark) {
                            Text("${dep.lagDays}д", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = AmberText)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Shared Components
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PortletCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
private fun StatusBadgeLarge(status: String) {
    val (bg, text) = when (status.lowercase()) {
        "in_progress", "в работе" -> BlueDark to BlueText
        "completed", "завершён", "завершен" -> EmeraldDark to EmeraldText
        "on_hold", "на паузе", "приостановлен" -> AmberDark to AmberText
        "cancelled", "отменён", "отменен" -> Color(0xFF3D1515) to Color(0xFFE88080)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = text,
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Header
// ═══════════════════════════════════════════════════════════════

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
                    Badge(bundle.detail.typeName ?: bundle.detail.typeId)
                    Badge(bundle.detail.status)
                }
            }
        }
        val crumbs = bundle.ancestors.joinToString(" / ") { it.name }
        if (crumbs.isNotBlank()) {
            Text(crumbs, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Owner / Assignee
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            bundle.detail.ownerName?.takeIf { it.isNotBlank() }?.let { PersonChip("Владелец", it) }
            bundle.detail.assigneeName?.takeIf { it.isNotBlank() }?.let { PersonChip("Исполнитель", it) }
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
private fun PersonChip(role: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(shape = CircleShape, color = BlueDark, modifier = Modifier.size(24.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    name.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueText,
                )
            }
        }
        Column {
            Text(role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun Badge(label: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun ActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
    )
}

// ═══════════════════════════════════════════════════════════════
// Participants Section
// ═══════════════════════════════════════════════════════════════

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

    PortletCard(title = "Участники (${participants.size})") {
        if (participants.isEmpty()) {
            Text("Нет участников", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        if (availableMembers.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.PersonAddAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("Добавить участника", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            MemberPicker(selectedUserId = selectedUserId, members = availableMembers, onSelect = { selectedUserId = it })
            RolePicker(currentRole = newRole, onSelect = { newRole = it })
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

@Composable
private fun ParticipantRow(
    participant: ParticipantDto,
    isSaving: Boolean,
    onUpdateParticipantRole: (String, String) -> Unit,
    onRemoveParticipant: (String) -> Unit,
) {
    var role by remember(participant.userId, participant.role) { mutableStateOf(participant.role) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(participant.userName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(participant.userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RolePicker(currentRole = role, onSelect = { role = it })
        if (role != participant.role) {
            Button(onClick = { onUpdateParticipantRole(participant.userId, role) }, enabled = !isSaving) { Text("OK") }
        }
        IconButton(onClick = { onRemoveParticipant(participant.userId) }, enabled = !isSaving) {
            Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MemberPicker(selectedUserId: String, members: List<WorkspaceMemberDto>, onSelect: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = members.filter {
        query.isBlank() || it.email.contains(query, ignoreCase = true) ||
            listOfNotNull(it.firstName, it.lastName).joinToString(" ").contains(query, ignoreCase = true)
    }
    OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Поиск") }, singleLine = true)
    filtered.take(5).forEach { member ->
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onSelect(member.id) },
            shape = RoundedCornerShape(8.dp),
            color = if (member.id == selectedUserId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                "${listOfNotNull(member.firstName, member.lastName).joinToString(" ")} (${member.email})",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RolePicker(currentRole: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("member", "manager", "executor").forEach { role ->
            Surface(
                modifier = Modifier.clickable { onSelect(role) },
                shape = RoundedCornerShape(6.dp),
                color = if (role == currentRole) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    role,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (role == currentRole) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
