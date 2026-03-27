package ru.custle.mobile.feature.todos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.TodoDto
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog

private val BlueDark = Color(0xFF1A2A4D)
private val BlueText = Color(0xFF8DB0F0)
private val EmeraldDark = Color(0xFF1A3D2A)
private val EmeraldText = Color(0xFF6FD4A0)

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
            message = "Задача \"${todo.title}\" будет удалена.",
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            TodoHeader(total = todos.size, completed = doneCount)
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
            item { EmptyTodoState() }
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

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun TodoHeader(total: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Мои задачи",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CountBadge(
                label = "$completed/$total",
                background = if (completed == total && total > 0) EmeraldDark else BlueDark,
                textColor = if (completed == total && total > 0) EmeraldText else BlueText,
            )
        }
    }
}

@Composable
private fun CountBadge(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Задача") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = onDueDateChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Срок") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
            ObjectPicker(
                selectedObjectId = selectedObjectId,
                projectTree = projectTree,
                onSelect = onObjectSelect,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onCreate,
                    enabled = title.isNotBlank() && !isBusy,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBusy) "..." else "Создать")
                }
                if (selectedObjectId.isNotBlank()) {
                    TextButton(onClick = onClearObject) {
                        Text("Сбросить")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTodoState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            "Задач пока нет",
            modifier = Modifier.padding(20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                IconButton(
                    onClick = { onToggle(todo.id) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (todo.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = null,
                        tint = if (todo.isDone) EmeraldText else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val meta = listOfNotNull(
                        todo.dueDate,
                        todo.objectName,
                    ).joinToString(" \u00B7 ")
                    if (meta.isNotBlank()) {
                        Text(
                            meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (!todo.objectId.isNullOrBlank()) {
                    IconButton(
                        onClick = { onOpenObject(todo.objectId) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                IconButton(
                    onClick = { editing = !editing },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(
                    onClick = { onDelete(todo.id) },
                    enabled = !isBusy,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (editing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                Text(if (isBusy) "..." else "Сохранить")
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Объект") },
        )
        if (selected != null) {
            Box(
                modifier = Modifier
                    .background(BlueDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    selected.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = BlueText,
                )
            }
        }
        filtered.forEach { node ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(node.id) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        node.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val info = listOfNotNull(node.typeName, node.status).joinToString(" \u00B7 ")
                    if (info.isNotBlank()) {
                        Text(
                            info,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (filtered.isEmpty() && query.isNotBlank()) {
            Text(
                "Не найдено",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
