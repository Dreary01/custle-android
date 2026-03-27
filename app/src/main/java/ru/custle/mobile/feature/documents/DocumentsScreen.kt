@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.documents

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.DocumentIntentHelper
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.DocumentFileDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.AppSectionCard
import ru.custle.mobile.core.ui.components.DestructiveConfirmDialog
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DocumentsRoute(
    objectId: String,
    objectName: String? = null,
    onBack: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val vm: DocumentsViewModel = viewModel(factory = DocumentsViewModelFactory(container))
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) vm.upload(uri)
    }

    LaunchedEffect(objectId, objectName) {
        vm.load(objectId, objectName)
    }

    DocumentsScreen(
        state = state,
        onBack = onBack,
        onRefresh = vm::refresh,
        onDownload = vm::download,
        onUpload = { uploadLauncher.launch("*/*") },
        onOpen = { file, path -> openDocument(context, file, path) },
        onShare = { file, path -> shareDocument(context, file, path) },
        onRename = vm::rename,
        onDelete = vm::delete,
        onReindex = vm::reindex,
    )
}

@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onDownload: (DocumentFileDto) -> Unit,
    onUpload: () -> Unit,
    onOpen: (DocumentFileDto, String) -> Unit,
    onShare: (DocumentFileDto, String) -> Unit,
    onRename: (DocumentFileDto, String) -> Unit,
    onDelete: (DocumentFileDto) -> Unit,
    onReindex: () -> Unit,
) {
    val indexedCount = state.indexedCount
    val totalCount = state.files.size
    var confirmDeleteFile by remember { mutableStateOf<DocumentFileDto?>(null) }

    confirmDeleteFile?.let { file ->
        DestructiveConfirmDialog(
            title = "Удалить документ?",
            message = "Файл ${fileName(file)} будет удалён.",
            isBusy = state.mutatingDocId == file.docId,
            onConfirm = {
                onDelete(file)
                confirmDeleteFile = null
            },
            onDismiss = { confirmDeleteFile = null },
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            DocumentsHero(
                objectName = state.objectName,
                totalCount = totalCount,
                indexedCount = indexedCount,
                pendingCount = state.pendingCount,
                canUpload = state.canUpload,
                isReindexing = state.isReindexing,
                uploadingFileName = state.uploadingFileName,
                onBack = onBack,
                onRefresh = onRefresh,
                onReindex = onReindex,
                onUpload = onUpload,
            )
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let {
            item { ErrorBanner(it) }
        }
        if (state.isLoading || state.isReindexing) {
            item {
                AppSectionCard(
                    title = if (state.isReindexing) "Идёт индексация" else "Загрузка",
                ) {
                    Text(
                        text = if (state.isReindexing && state.queuedForReindex > 0) "Ещё ${state.queuedForReindex} файлов." else "Обновление списка...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (state.downloadedPath != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Скачанный файл", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            state.downloadedPath,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (state.files.isEmpty() && !state.isLoading) {
            item {
                EmptyStateCard(
                    title = "Документов нет",
                    message = "Загруженные файлы появятся здесь.",
                )
            }
        } else {
            items(state.files, key = { it.docId }) { file ->
                DocumentRow(
                    file = file,
                    indexed = state.indexedDocIds.contains(file.docId),
                    isDownloaded = state.downloadedDocId == file.docId && !state.downloadedPath.isNullOrBlank(),
                    isDownloading = state.downloadingDocId == file.docId,
                    isMutating = state.mutatingDocId == file.docId,
                    canRename = state.canRename,
                    canDelete = state.canDelete,
                    onDownload = { onDownload(file) },
                    onOpen = { state.downloadedPath?.let { onOpen(file, it) } },
                    onShare = { state.downloadedPath?.let { onShare(file, it) } },
                    onRename = { newName -> onRename(file, newName) },
                    onDelete = { confirmDeleteFile = file },
                )
            }
        }
    }
}

@Composable
private fun DocumentsHero(
    objectName: String?,
    totalCount: Int,
    indexedCount: Int,
    pendingCount: Int,
    canUpload: Boolean,
    isReindexing: Boolean,
    uploadingFileName: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onReindex: () -> Unit,
    onUpload: () -> Unit,
) {
    AppHeroCard(
        title = objectName ?: "Документы",
        subtitle = "$totalCount файлов, $indexedCount индексировано",
        chips = buildList {
            add("$totalCount файлов" to Icons.Outlined.FolderCopy)
            add("$indexedCount индекс." to Icons.Outlined.TaskAlt)
            if (pendingCount > 0) add("$pendingCount ждут" to Icons.Outlined.Description)
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                Text("Назад")
            }
            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Text("Обновить")
            }
            if (pendingCount > 0) {
                Button(onClick = onReindex, enabled = !isReindexing) {
                    Text(if (isReindexing) "Индексация..." else "Переиндексировать")
                }
            }
            if (canUpload) {
                Button(onClick = onUpload, enabled = uploadingFileName == null) {
                    Icon(Icons.Outlined.FileUpload, contentDescription = null)
                    Text(if (uploadingFileName == null) "Загрузить" else "Загрузка...")
                }
            }
        }
        uploadingFileName?.let {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    "Загружается: $it",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DocumentRow(
    file: DocumentFileDto,
    indexed: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    isMutating: Boolean,
    canRename: Boolean,
    canDelete: Boolean,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var isRenaming by remember(file.docId) { mutableStateOf(false) }
    var draftName by remember(file.docId, file.id) { mutableStateOf(fileName(file)) }

    DsCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = fileName(file),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusBadge(if (indexed) "Индексирован" else "Ждёт индексации")
                formatDate(file.date)?.let { StatusBadge(it) }
                StatusBadge(formatSize(file.size))
            }
            val meta = buildList {
                file.objectName?.let { add(it) }
                file.authorName?.let { add(it) }
            }.joinToString(" / ")
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
                Button(onClick = onDownload, enabled = !isDownloading && !isMutating) {
                    Text(if (isDownloading) "Скачивание..." else "Скачать")
                }
                if (isDownloaded) {
                    OutlinedButton(onClick = onOpen, enabled = !isMutating) {
                        Text("Открыть")
                    }
                    OutlinedButton(onClick = onShare, enabled = !isMutating) {
                        Icon(Icons.Outlined.Share, contentDescription = null)
                        Text("Поделиться")
                    }
                }
                if (canRename && !isRenaming) {
                    OutlinedButton(
                        onClick = {
                            draftName = fileName(file)
                            isRenaming = true
                        },
                        enabled = !isMutating,
                    ) {
                        Text("Переименовать")
                    }
                }
                if (canDelete) {
                    OutlinedButton(onClick = onDelete, enabled = !isMutating) {
                        Text(if (isMutating) "Удаление..." else "Удалить")
                    }
                }
            }
            if (isRenaming) {
                AppSectionCard(title = "Переименование") {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Новое имя") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isMutating,
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onRename(draftName)
                                isRenaming = false
                            },
                            enabled = !isMutating && draftName.isNotBlank() && draftName != fileName(file),
                        ) {
                            Text(if (isMutating) "Сохранение..." else "Сохранить")
                        }
                        OutlinedButton(
                            onClick = {
                                draftName = fileName(file)
                                isRenaming = false
                            },
                            enabled = !isMutating,
                        ) {
                            Text("Отмена")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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

private fun fileName(file: DocumentFileDto): String = file.id.substringAfterLast('/').ifBlank { file.docId }

private fun formatSize(size: Long): String {
    if (size <= 0L) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024.0
    val gb = mb * 1024.0
    return when {
        size >= gb -> String.format(Locale.US, "%.1f GB", size / gb)
        size >= mb -> String.format(Locale.US, "%.1f MB", size / mb)
        size >= kb -> String.format(Locale.US, "%.1f KB", size / kb)
        else -> "$size B"
    }
}

private fun formatDate(value: String): String? {
    if (value.isBlank()) return null
    return runCatching {
        OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }.getOrNull()
}

private fun openDocument(context: android.content.Context, file: DocumentFileDto, path: String) {
    val source = File(path)
    val intent = DocumentIntentHelper.createOpenIntent(context, source, fileName(file))
    if (intent == null) {
        toast(context, "Не удалось подготовить файл для открытия")
        return
    }
    runCatching {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        toast(context, "Нет приложения для открытия этого файла")
    }
}

private fun shareDocument(context: android.content.Context, file: DocumentFileDto, path: String) {
    val source = File(path)
    val intent = DocumentIntentHelper.createShareIntent(context, source, fileName(file)) ?: run {
        toast(context, "Не удалось подготовить файл для отправки")
        return
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Поделиться документом").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        toast(context, "Не удалось открыть меню отправки")
    }
}

private fun toast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
