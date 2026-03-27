@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.todos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.TodoDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog
import ru.custle.mobile.core.ui.components.EmptyStateCard

@Composable
fun TodosScreen(
    todos: List<TodoDto>,
    projectTree: List<ObjectNodeDto>,
    isBusy: Boolean,
    onCreate: (String, String?, String?) -> Unit,
    onUpdate: (String, String, String?, String?, Boolean?) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpenObject: (String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var selectedObjectId by rememberSaveable { mutableStateOf("") }
    var confirmDeleteTodo by remember { mutableStateOf<TodoDto?>(null) }
    val doneCount = todos.count { it.isDone }

    confirmDeleteTodo?.let { todo ->
        DestructiveConfirmDialog(
            title = "Удалить задачу?",
            message = "Задача \"${todo.title}\" будет удалена из твоего рабочего списка.",
            isBusy = isBusy,
            onConfirm = {
                onDelete(todo.id)
                confirmDeleteTodo = null
            },
            onDismiss = { confirmDeleteTodo = null },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TodoHero(
                total = todos.size,
                completed = doneCount,
            )
        }
        item {
            TodoComposer(
                title = title,
                dueDate = dueDate,
                selectedObjectId = selectedObjectId,
                projectTree = projectTree,
                isBusy = isBusy,
                onTitleChange = { title = it },
                onDueDateChange = { dueDate = it },
                onObjectSelect = { selectedObjectId = it },
                onClearObject = { selectedObjectId = "" },
                onCreate = {
                    onCreate(
                        title.trim(),
                        dueDate.takeIf { it.isNotBlank() },
                        selectedObjectId.takeIf { it.isNotBlank() },
                    )
                    title = ""
                    dueDate = ""
                    selectedObjectId = ""
                },
            )
        }
        if (todos.isEmpty()) {
            item {
                EmptyTodoState()
            }
        } else {
            items(todos, key = { it.id }) { todo ->
                TodoCard(
                    todo = todo,
                    projectTree = projectTree,
                    isBusy = isBusy,
                    onUpdate = onUpdate,
                    onToggle = onToggle,
                    onDelete = { id -> confirmDeleteTodo = todos.firstOrNull { it.id == id } },
                    onOpenObject = onOpenObject,
                )
            }
        }
    }
}

@Composable
private fun TodoHero(
    total: Int,
    completed: Int,
) {
    AppHeroCard(
        title = "Мои задачи",
        subtitle = "Здесь лучше держать короткий операционный контур: что нужно сделать, к какому объекту это относится и какой срок горит.",
        chips = listOf(
            "$total всего" to Icons.Outlined.TaskAlt,
            "$completed закрыто" to Icons.Outlined.Visibility,
            "${total - completed} в работе" to Icons.Outlined.CalendarToday,
        ),
    )
}

@Composable
private fun TodoComposer(
    title: String,
    dueDate: String,
    selectedObjectId: String,
    projectTree: List<ObjectNodeDto>,
    isBusy: Boolean,
    onTitleChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onObjectSelect: (String) -> Unit,
    onClearObject: () -> Unit,
    onCreate: () -> Unit,
) {
    AppSectionCard(
        title = "Новая задача",
        hint = "Сначала сформулируй действие, потом при необходимости привяжи его к объекту.",
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Что нужно сделать") },
        )
        OutlinedTextField(
            value = dueDate,
            onValueChange = onDueDateChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Срок, например 2026-03-30") },
        )
        ObjectPicker(
            selectedObjectId = selectedObjectId,
            projectTree = projectTree,
            onSelect = onObjectSelect,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onCreate,
                enabled = title.isNotBlank() && !isBusy,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isBusy) "Сохранение..." else "Создать задачу")
            }
            if (selectedObjectId.isNotBlank()) {
                TextButton(onClick = onClearObject) {
                    Text("Убрать объект")
                }
            }
        }
    }
}

@Composable
private fun EmptyTodoState() {
    EmptyStateCard(
        title = "Пока пусто",
        message = "Начни с одной короткой задачи. Мобильный сценарий тут должен быть быстрым, а не бюрократическим.",
    )
}

@Composable
private fun TodoCard(
    todo: TodoDto,
    projectTree: List<ObjectNodeDto>,
    isBusy: Boolean,
    onUpdate: (String, String, String?, String?, Boolean?) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onOpenObject: (String) -> Unit,
) {
    var editing by remember(todo.id) { mutableStateOf(false) }
    var title by remember(todo.id, todo.title) { mutableStateOf(todo.title) }
    var dueDate by remember(todo.id, todo.dueDate) { mutableStateOf(todo.dueDate.orEmpty()) }
    var objectId by remember(todo.id, todo.objectId) { mutableStateOf(todo.objectId.orEmpty()) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(checked = todo.isDone, onCheckedChange = { onToggle(todo.id) })
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                    )
                    val meta = listOfNotNull(todo.objectName, todo.dueDate).joinToString(" • ")
                    if (meta.isNotBlank()) {
                        Text(
                            meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (!todo.objectId.isNullOrBlank()) {
                            AssistChip(
                                onClick = { onOpenObject(todo.objectId) },
                                label = { Text("Открыть объект") },
                                leadingIcon = { Icon(Icons.Outlined.Visibility, contentDescription = null) },
                            )
                        }
                        AssistChip(
                            onClick = { editing = !editing },
                            label = { Text(if (editing) "Свернуть" else "Редактировать") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        )
                    }
                }
                IconButton(onClick = { onDelete(todo.id) }, enabled = !isBusy) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                }
            }
            if (editing) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Заголовок") },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Срок") },
                            singleLine = true,
                        )
                        ObjectPicker(
                            selectedObjectId = objectId,
                            projectTree = projectTree,
                            onSelect = { objectId = it },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    onUpdate(
                                        todo.id,
                                        title.trim(),
                                        dueDate.takeIf { it.isNotBlank() },
                                        objectId.takeIf { it.isNotBlank() },
                                        todo.isDone,
                                    )
                                    editing = false
                                },
                                enabled = title.isNotBlank() && !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(if (isBusy) "Сохранение..." else "Сохранить")
                            }
                            TextButton(onClick = { editing = false }, enabled = !isBusy) {
                                Text("Отмена")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectPicker(
    selectedObjectId: String,
    projectTree: List<ObjectNodeDto>,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val selected = projectTree.firstOrNull { it.id == selectedObjectId }
    val filtered = projectTree.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true)
    }.take(6)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Привязать к объекту") },
        )
        if (selected != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    "Выбран объект: ${selected.name}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        filtered.forEach { node ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(node.id) },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(node.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        listOfNotNull(node.typeName, node.status).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (filtered.isEmpty() && query.isNotBlank()) {
            Text(
                "По этому запросу объект не найден",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TodoStatChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
    )
}
