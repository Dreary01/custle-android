package ru.custle.mobile.feature.knowledge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer

@Composable
fun KnowledgeBaseRoute(
    initialNoteId: String? = null,
    initialArticleId: String? = null,
    onInitialNavigationConsumed: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val factory = remember(container) { KnowledgeBaseViewModelFactory(container) }
    val viewModel: KnowledgeBaseViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialNoteId, initialArticleId) {
        when {
            !initialNoteId.isNullOrBlank() -> {
                viewModel.openNote(initialNoteId)
                onInitialNavigationConsumed()
            }
            !initialArticleId.isNullOrBlank() -> {
                viewModel.openArticle(initialArticleId)
                onInitialNavigationConsumed()
            }
        }
    }

    KnowledgeBaseScreen(
        state = state,
        onTabChange = viewModel::selectTab,
        onNotesFilterChange = viewModel::selectNotesFilter,
        onOpenNote = viewModel::openNote,
        onOpenArticle = viewModel::openArticle,
        onStartCreateNote = viewModel::startCreateNote,
        onStartEditNote = viewModel::startEditNote,
        onUpdateNoteDraft = viewModel::updateNoteDraft,
        onSaveNote = viewModel::saveNote,
        onDeleteNote = viewModel::deleteSelectedNote,
        onStartCreateArticle = viewModel::startCreateArticle,
        onStartEditArticle = viewModel::startEditArticle,
        onUpdateArticleDraft = viewModel::updateArticleDraft,
        onSaveArticle = viewModel::saveArticle,
        onDeleteArticle = viewModel::deleteSelectedArticle,
        onCancelEditor = viewModel::cancelEditor,
        onCloseDetail = viewModel::closeDetail,
        onRefresh = viewModel::refresh,
    )
}
