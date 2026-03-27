package ru.custle.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.custle.mobile.core.data.LocalAppContainer
import ru.custle.mobile.navigation.CustleNavHost
import ru.custle.mobile.navigation.CustleNavigatorViewModel
import ru.custle.mobile.navigation.CustleNavigatorViewModelFactory

@Composable
fun CustleApp(
    pendingDeepLink: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    val container = LocalAppContainer.current
    val factory = remember(container) { CustleNavigatorViewModelFactory(container) }
    val viewModel: CustleNavigatorViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(pendingDeepLink) {
        if (!pendingDeepLink.isNullOrBlank() && viewModel.handleOAuthNavigation(pendingDeepLink)) {
            onDeepLinkConsumed()
        }
    }

    CustleNavHost(
        state = state,
        onLogin = viewModel::login,
        onLoginWithYandex = viewModel::startYandexAuth,
        onCancelAuthWebView = viewModel::cancelAuthWebView,
        onAuthWebNavigation = viewModel::handleOAuthNavigation,
        onConsumeAuthLaunch = viewModel::cancelAuthWebView,
        onSelectWorkspace = viewModel::selectWorkspace,
        onAcceptWorkspaceInvitation = viewModel::acceptWorkspaceInvitation,
        onRefresh = viewModel::refreshDashboard,
        onSelectSection = viewModel::selectSection,
        onCreateTodo = viewModel::addTodo,
        onUpdateTodo = viewModel::updateTodo,
        onToggleTodo = viewModel::toggleTodo,
        onDeleteTodo = viewModel::deleteTodo,
        onSearch = viewModel::search,
        onOpenSearchResult = viewModel::openSearchResult,
        onClearKnowledgeDeepLink = viewModel::clearKnowledgeDeepLink,
        onSaveProfile = viewModel::updateProfile,
        onInviteWorkspaceMember = viewModel::inviteWorkspaceMember,
        onUpdateWorkspaceMemberRole = viewModel::updateWorkspaceMemberRole,
        onRemoveWorkspaceMember = viewModel::removeWorkspaceMember,
        onGenerateTelegramCode = viewModel::generateTelegramCode,
        onUpdateTelegramAutoDelete = viewModel::updateTelegramAutoDelete,
        onOpenObject = viewModel::openObject,
        onOpenSelectedObjectDocuments = viewModel::openSelectedObjectDocuments,
        onOpenSelectedObjectDiscussions = viewModel::openSelectedObjectDiscussions,
        onOpenSelectedObjectTemplates = viewModel::openSelectedObjectTemplates,
        onAddSelectedObjectParticipant = viewModel::addSelectedObjectParticipant,
        onUpdateSelectedObjectParticipantRole = viewModel::updateSelectedObjectParticipantRole,
        onRemoveSelectedObjectParticipant = viewModel::removeSelectedObjectParticipant,
        onCloseObject = viewModel::closeObject,
        onCloseSelectedObjectDocuments = viewModel::closeSelectedObjectDocuments,
        onCloseSelectedObjectDiscussions = viewModel::closeSelectedObjectDiscussions,
        onCloseSelectedObjectTemplates = viewModel::closeSelectedObjectTemplates,
        onClearSelectedDiscussionId = viewModel::clearSelectedDiscussionId,
        onLogout = viewModel::logout,
    )
}
