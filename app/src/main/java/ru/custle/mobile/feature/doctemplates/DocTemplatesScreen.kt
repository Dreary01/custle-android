@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package ru.custle.mobile.feature.doctemplates

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.DocumentIntentHelper
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.core.model.DocTemplateDto
import ru.custle.mobile.core.ui.components.AppHeroCard
import ru.custle.mobile.core.ui.components.EmptyStateCard
import ru.custle.mobile.core.ui.components.ErrorBanner
import java.io.File

data class DocTemplatesUiState(
    val objectId: String? = null,
    val objectName: String? = null,
    val objectTypeId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<DocTemplateDto> = emptyList(),
    val selected: DocTemplateDto? = null,
    val downloadingTemplateId: String? = null,
    val downloadedTemplateId: String? = null,
    val downloadedPath: String? = null,
    val generatingTemplateId: String? = null,
    val generatedTemplateId: String? = null,
    val generatedPath: String? = null,
)

class DocTemplatesViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(DocTemplatesUiState())
    val state: StateFlow<DocTemplatesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun load(
        objectId: String? = null,
        objectName: String? = null,
        objectTypeId: String? = null,
    ) {
        _state.value = _state.value.copy(
            objectId = objectId,
            objectName = objectName,
            objectTypeId = objectTypeId,
        )
        refresh()
    }

    fun refresh() {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val items = container.docTemplateRepository.list(objectTypeId = _state.value.objectTypeId)
            val selectedId = _state.value.selected?.id
            _state.value = _state.value.copy(
                isLoading = false,
                items = items,
                selected = items.firstOrNull { it.id == selectedId } ?: _state.value.selected,
            )
        }
    }

    fun generate(template: DocTemplateDto, format: String? = null) {
        val objectId = _state.value.objectId ?: return
        launchCatching {
            _state.value = _state.value.copy(generatingTemplateId = template.id, errorMessage = null)
            val localFile = withContext(Dispatchers.IO) {
                container.docTemplateRepository.generate(
                    objectId = objectId,
                    templateId = template.id,
                    fileName = generatedFileName(template, format),
                    format = format,
                )
            }
            _state.value = _state.value.copy(
                generatingTemplateId = null,
                generatedTemplateId = template.id,
                generatedPath = localFile.absolutePath,
            )
        }
    }

    fun open(id: String) {
        launchCatching {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val detail = container.docTemplateRepository.detail(id)
            _state.value = _state.value.copy(isLoading = false, selected = detail)
        }
    }

    fun close() {
        _state.value = _state.value.copy(selected = null)
    }

    fun download(template: DocTemplateDto) {
        launchCatching {
            _state.value = _state.value.copy(downloadingTemplateId = template.id, errorMessage = null)
            val localFile = withContext(Dispatchers.IO) {
                container.docTemplateRepository.download(template.id, templateFileName(template))
            }
            _state.value = _state.value.copy(
                downloadingTemplateId = null,
                downloadedTemplateId = template.id,
                downloadedPath = localFile.absolutePath,
            )
        }
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        downloadingTemplateId = null,
                        errorMessage = error.message ?: "Неизвестная ошибка",
                    )
                }
        }
    }
}

class DocTemplatesViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocTemplatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DocTemplatesViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun DocTemplatesRoute() {
    val container = LocalAppContainer.current
    val factory = remember(container) { DocTemplatesViewModelFactory(container) }
    val viewModel: DocTemplatesViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    DocTemplatesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpen = viewModel::open,
        onClose = viewModel::close,
        onDownload = viewModel::download,
        onGenerateDocx = { viewModel.generate(it, "docx") },
        onGeneratePdf = { viewModel.generate(it, "pdf") },
        onOpenFile = { template, path -> openTemplate(context, template, path) },
        onShareFile = { template, path -> shareTemplate(context, template, path) },
        onBack = null,
    )
}

@Composable
fun DocTemplatesRoute(
    objectId: String,
    objectName: String,
    objectTypeId: String? = null,
    onBack: () -> Unit,
) {
    val container = LocalAppContainer.current
    val factory = remember(container) { DocTemplatesViewModelFactory(container) }
    val viewModel: DocTemplatesViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(objectId, objectTypeId) {
        viewModel.load(
            objectId = objectId,
            objectName = objectName,
            objectTypeId = objectTypeId,
        )
    }

    DocTemplatesScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpen = viewModel::open,
        onClose = viewModel::close,
        onDownload = viewModel::download,
        onGenerateDocx = { viewModel.generate(it, "docx") },
        onGeneratePdf = { viewModel.generate(it, "pdf") },
        onOpenFile = { template, path -> openTemplate(context, template, path) },
        onShareFile = { template, path -> shareTemplate(context, template, path) },
        onBack = onBack,
    )
}

@Composable
fun DocTemplatesScreen(
    state: DocTemplatesUiState,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
    onClose: () -> Unit,
    onDownload: (DocTemplateDto) -> Unit,
    onGenerateDocx: (DocTemplateDto) -> Unit,
    onGeneratePdf: (DocTemplateDto) -> Unit,
    onOpenFile: (DocTemplateDto, String) -> Unit,
    onShareFile: (DocTemplateDto, String) -> Unit,
    onBack: (() -> Unit)?,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TemplatesHero(
                objectName = state.objectName,
                canGenerate = state.objectId != null,
                itemsCount = state.items.size,
                onRefresh = onRefresh,
                onBack = onBack,
            )
        }
        state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
            item { ErrorBanner(message) }
        }
        state.selected?.let { template ->
            item {
                DsCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(template.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        val meta = listOfNotNull(
                            template.objectTypeName,
                            template.filePath?.substringAfterLast('/'),
                            template.updatedAt.takeIf { it.isNotBlank() },
                        ).joinToString(" / ")
                        if (meta.isNotBlank()) {
                            Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { onDownload(template) },
                                enabled = state.downloadingTemplateId != template.id,
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = null)
                                Text(if (state.downloadingTemplateId == template.id) "Скачивание..." else "Скачать")
                            }
                            if (state.objectId != null) {
                                Button(
                                    onClick = { onGenerateDocx(template) },
                                    enabled = state.generatingTemplateId != template.id,
                                ) {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                                    Text(if (state.generatingTemplateId == template.id) "Генерация..." else "DOCX")
                                }
                                OutlinedButton(
                                    onClick = { onGeneratePdf(template) },
                                    enabled = state.generatingTemplateId != template.id,
                                ) {
                                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                                    Text("PDF")
                                }
                            }
                            if (state.downloadedTemplateId == template.id && !state.downloadedPath.isNullOrBlank()) {
                                OutlinedButton(onClick = { onOpenFile(template, state.downloadedPath) }) { Text("Открыть") }
                                OutlinedButton(onClick = { onShareFile(template, state.downloadedPath) }) { Text("Поделиться") }
                            }
                            if (state.generatedTemplateId == template.id && !state.generatedPath.isNullOrBlank()) {
                                OutlinedButton(onClick = { onOpenFile(template, state.generatedPath) }) { Text("Открыть результат") }
                                OutlinedButton(onClick = { onShareFile(template, state.generatedPath) }) { Text("Поделиться результатом") }
                            }
                            OutlinedButton(onClick = onClose) { Text("Свернуть") }
                        }
                    }
                }
            }
        }
        if (state.items.isEmpty() && !state.isLoading) {
            item {
                EmptyStateCard(
                    title = "Шаблонов нет",
                    message = "Шаблоны документов появятся здесь.",
                )
            }
        } else {
            items(state.items, key = { it.id }) { item ->
                DocTemplateRow(
                    item = item,
                    isSelected = state.selected?.id == item.id,
                    isDownloading = state.downloadingTemplateId == item.id,
                    isDownloaded = state.downloadedTemplateId == item.id && !state.downloadedPath.isNullOrBlank(),
                    isGenerating = state.generatingTemplateId == item.id,
                    isGenerated = state.generatedTemplateId == item.id && !state.generatedPath.isNullOrBlank(),
                    canGenerate = state.objectId != null,
                    onOpen = onOpen,
                    onDownload = { onDownload(item) },
                    onGenerateDocx = { onGenerateDocx(item) },
                    onGeneratePdf = { onGeneratePdf(item) },
                    onOpenFile = { state.downloadedPath?.let { onOpenFile(item, it) } },
                    onShareFile = { state.downloadedPath?.let { onShareFile(item, it) } },
                    onOpenGenerated = { state.generatedPath?.let { onOpenFile(item, it) } },
                    onShareGenerated = { state.generatedPath?.let { onShareFile(item, it) } },
                )
            }
        }
    }
}

@Composable
private fun DocTemplateRow(
    item: DocTemplateDto,
    isSelected: Boolean,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    isGenerating: Boolean,
    isGenerated: Boolean,
    canGenerate: Boolean,
    onOpen: (String) -> Unit,
    onDownload: () -> Unit,
    onGenerateDocx: () -> Unit,
    onGeneratePdf: () -> Unit,
    onOpenFile: () -> Unit,
    onShareFile: () -> Unit,
    onOpenGenerated: () -> Unit,
    onShareGenerated: () -> Unit,
) {
    DsCard(modifier = Modifier.clickable { onOpen(item.id) }) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            val meta = listOfNotNull(
                item.objectTypeName,
                item.filePath?.substringAfterLast('/'),
                item.updatedAt.takeIf { it.isNotBlank() },
            ).joinToString(" / ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onDownload, enabled = !isDownloading) {
                    Icon(Icons.Outlined.Download, contentDescription = null)
                    Text(if (isDownloading) "Скачивание..." else "Скачать")
                }
                if (canGenerate) {
                    Button(onClick = onGenerateDocx, enabled = !isGenerating) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                        Text(if (isGenerating) "Генерация..." else "DOCX")
                    }
                    OutlinedButton(onClick = onGeneratePdf, enabled = !isGenerating) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                        Text("PDF")
                    }
                }
                if (isDownloaded) {
                    OutlinedButton(onClick = onOpenFile) { Text("Открыть") }
                    OutlinedButton(onClick = onShareFile) { Text("Поделиться") }
                }
                if (isGenerated) {
                    OutlinedButton(onClick = onOpenGenerated) { Text("Открыть результат") }
                    OutlinedButton(onClick = onShareGenerated) { Text("Поделиться результатом") }
                }
            }
            if (isSelected) {
                Text("Выбрано", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun TemplatesHero(
    objectName: String?,
    canGenerate: Boolean,
    itemsCount: Int,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
) {
    AppHeroCard(
        title = objectName ?: "Шаблоны документов",
        subtitle = "$itemsCount шаблонов${if (canGenerate) ", генерация доступна" else ""}",
        chips = buildList {
            add("$itemsCount шаблонов" to Icons.Outlined.AutoAwesome)
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            onBack?.let {
                OutlinedButton(onClick = it) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    Text("Назад")
                }
            }
            Button(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Text("Обновить")
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

private fun templateFileName(template: DocTemplateDto): String =
    template.filePath?.substringAfterLast('/')?.ifBlank { "${template.name}.docx" }
        ?: "${template.name}.docx"

private fun generatedFileName(template: DocTemplateDto, format: String?): String {
    val ext = if (format == "pdf") "pdf" else "docx"
    return "${template.name}.$ext"
}

private fun openTemplate(context: android.content.Context, template: DocTemplateDto, path: String) {
    val source = File(path)
    val displayName = source.name.ifBlank { templateFileName(template) }
    val intent = DocumentIntentHelper.createOpenIntent(context, source, displayName)
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

private fun shareTemplate(context: android.content.Context, template: DocTemplateDto, path: String) {
    val source = File(path)
    val displayName = source.name.ifBlank { templateFileName(template) }
    val intent = DocumentIntentHelper.createShareIntent(context, source, displayName) ?: run {
        toast(context, "Не удалось подготовить файл для отправки")
        return
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Поделиться шаблоном").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        toast(context, "Не удалось открыть меню отправки")
    }
}

private fun toast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
