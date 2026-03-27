package ru.custle.mobile.navigation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.custle.mobile.core.data.AppContainer
import ru.custle.mobile.core.data.toDebugMessage

class CustleNavigatorViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _state = MutableStateFlow(CustleUiState())
    val state: StateFlow<CustleUiState> = _state.asStateFlow()

    init {
        bootstrap()
    }

    fun login(email: String, password: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null, authWebUrl = null)
            val user = runCatching { container.authRepository.login(email, password) }
                .getOrElse { error("Login failed\n${it.toDebugMessage("Step: POST /auth/login")}") }
            completeAuthorizedSession(user, "password")
        }
    }

    fun startYandexAuth() {
        _state.value = _state.value.copy(
            errorMessage = null,
            authWebUrl = container.authRepository.yandexOAuthUrl(),
        )
    }

    fun cancelAuthWebView() {
        _state.value = _state.value.copy(authWebUrl = null)
    }

    fun handleOAuthNavigation(rawUrl: String): Boolean {
        val uri = Uri.parse(rawUrl)
        return when {
            container.authRepository.isMobileOAuthCallback(uri) -> {
                launchCatching {
                    _state.value = _state.value.copy(isBusy = true, errorMessage = null, authWebUrl = null)
                    val code = runCatching { container.authRepository.extractOAuthCode(uri) }
                        .getOrElse { error("Yandex OAuth callback failed\n${it.toDebugMessage("Step: parse custle://oauth-callback")}") }
                    val payload = runCatching { container.authRepository.completeMobileOAuth(code) }
                        .getOrElse { error("Yandex OAuth failed\n${it.toDebugMessage("Step: POST /auth/oauth/yandex/exchange")}") }
                    completeAuthorizedSession(payload.user, "yandex")
                }
                true
            }

            container.authRepository.isOAuthWebError(uri) -> {
                _state.value = _state.value.copy(
                    authWebUrl = null,
                    errorMessage = uri.getQueryParameter("error") ?: "Не удалось войти через Яндекс.",
                )
                true
            }

            else -> false
        }
    }

    fun selectWorkspace(workspaceId: String, source: String? = null) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            runCatching { container.authRepository.switchWorkspace(workspaceId) }
                .getOrElse {
                    error(
                        "Workspace selection failed\n" +
                            it.toDebugMessage("Source: ${source ?: "manual"}\nStep: POST /auth/switch-workspace"),
                    )
                }
            val user = runCatching { container.authRepository.me() }.getOrNull() ?: _state.value.user
            val dashboard = runCatching { container.dashboardRepository.snapshot() }
                .getOrElse {
                    error(
                        "Workspace dashboard bootstrap failed\n" +
                            it.toDebugMessage("Source: ${source ?: "manual"}\nStep: dashboard snapshot after switch-workspace"),
                    )
                }
            _state.value = _state.value.copy(
                isBusy = false,
                user = user,
                dashboard = dashboard,
                destination = RootDestination.Dashboard,
            )
        }
    }

    fun acceptWorkspaceInvitation(token: String) {
        if (token.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val workspaceId = container.authRepository.acceptInvitation(token.trim())
            val workspaces = container.authRepository.workspaces()
            _state.value = _state.value.copy(
                isBusy = false,
                workspaces = workspaces,
                destination = RootDestination.WorkspacePicker,
            )
            selectWorkspace(workspaceId)
        }
    }

    fun refreshDashboard() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val dashboard = container.dashboardRepository.snapshot()
            _state.value = _state.value.copy(isBusy = false, dashboard = dashboard)
        }
    }

    fun selectSection(section: MainSection) {
        _state.value = _state.value.copy(section = section)
        when (section) {
            MainSection.DASHBOARD -> Unit
            MainSection.INBOX -> Unit
            MainSection.PROJECTS -> loadProjectTree()
            MainSection.NEWS -> Unit
            MainSection.REPORTS -> loadReports()
            MainSection.TABLES -> loadRefTables()
            MainSection.SCHEMA -> loadSchema()
            MainSection.TEMPLATES -> loadDocumentTemplates()
            MainSection.LAYOUTS -> Unit
            MainSection.ADMIN -> Unit
            MainSection.MARKETPLACE -> Unit
            MainSection.SUPERADMIN -> Unit
            MainSection.WIDGETS -> Unit
            MainSection.KNOWLEDGE -> Unit
            MainSection.TODOS -> loadTodos()
            MainSection.SEARCH -> Unit
            MainSection.PROFILE -> loadProfileMeta()
        }
    }

    fun loadReports() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val reports = container.reportRepository.list()
            _state.value = _state.value.copy(
                isBusy = false,
                reports = reports,
                section = MainSection.REPORTS,
            )
        }
    }

    fun loadRefTables() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val refTables = container.refTableRepository.list()
            _state.value = _state.value.copy(
                isBusy = false,
                refTables = refTables,
                section = MainSection.TABLES,
            )
        }
    }

    fun loadSchema() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val bundle = container.schemaRepository.load()
            _state.value = _state.value.copy(
                isBusy = false,
                objectTypes = bundle.objectTypes,
                requisites = bundle.requisites,
                requisiteGroups = bundle.groups,
                section = MainSection.SCHEMA,
            )
        }
    }

    fun loadDocumentTemplates() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val templates = container.docTemplateRepository.list()
            _state.value = _state.value.copy(
                isBusy = false,
                documentTemplates = templates,
                section = MainSection.TEMPLATES,
            )
        }
    }

    fun loadTodos() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val (todos, tree) = coroutineScope {
                val todosDeferred = async { container.todoRepository.list() }
                val treeDeferred = async {
                    if (_state.value.projectTree.isEmpty()) {
                        runCatching { container.projectRepository.tree() }.getOrDefault(emptyList())
                    } else {
                        _state.value.projectTree
                    }
                }
                todosDeferred.await() to treeDeferred.await()
            }
            _state.value = _state.value.copy(isBusy = false, todos = todos, projectTree = tree)
        }
    }

    fun addTodo(title: String, dueDate: String? = null, objectId: String? = null) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            container.todoRepository.create(title = title, dueDate = dueDate, objectId = objectId)
            val todos = container.todoRepository.list()
            _state.value = _state.value.copy(isBusy = false, todos = todos, section = MainSection.TODOS)
        }
    }

    fun updateTodo(id: String, title: String, dueDate: String? = null, objectId: String? = null, isDone: Boolean? = null) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            container.todoRepository.update(
                id = id,
                title = title,
                dueDate = dueDate,
                objectId = objectId,
                isDone = isDone,
            )
            val todos = container.todoRepository.list()
            _state.value = _state.value.copy(isBusy = false, todos = todos, section = MainSection.TODOS)
        }
    }

    fun toggleTodo(id: String) {
        launchCatching {
            container.todoRepository.toggle(id)
            val todos = container.todoRepository.list()
            _state.value = _state.value.copy(todos = todos)
        }
    }

    fun deleteTodo(id: String) {
        launchCatching {
            container.todoRepository.delete(id)
            val todos = container.todoRepository.list()
            _state.value = _state.value.copy(todos = todos)
        }
    }

    fun search(query: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null, lastSearchQuery = query)
            val items = if (query.isBlank()) emptyList() else container.searchRepository.search(query.trim())
            _state.value = _state.value.copy(
                isBusy = false,
                searchResults = items,
                lastSearchQuery = query,
                section = MainSection.SEARCH,
            )
        }
    }

    fun openSearchResult(id: String, type: String) {
        when (type) {
            "object" -> openObject(id)
            "note" -> {
                _state.value = _state.value.copy(
                    section = MainSection.KNOWLEDGE,
                    knowledgeOpenNoteId = id,
                    knowledgeOpenArticleId = null,
                )
            }
            "article" -> {
                _state.value = _state.value.copy(
                    section = MainSection.KNOWLEDGE,
                    knowledgeOpenArticleId = id,
                    knowledgeOpenNoteId = null,
                )
            }
            "document" -> {
                _state.value = _state.value.copy(
                    errorMessage = "Поиск по документам уже работает, отдельный deep link на документ пока не подключён.",
                )
            }
            "discussion" -> {
                val parts = id.split(":", limit = 2)
                val objectId = parts.getOrNull(0).orEmpty()
                val discussionId = parts.getOrNull(1).orEmpty()
                if (objectId.isBlank() || discussionId.isBlank()) {
                    _state.value = _state.value.copy(
                        errorMessage = "Поиск вернул discussion без objectId/discussionId.",
                    )
                } else {
                    openObjectDiscussion(objectId = objectId, discussionId = discussionId)
                }
            }
        }
    }

    fun loadProjectTree() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val tree = container.projectRepository.tree()
            _state.value = _state.value.copy(
                isBusy = false,
                projectTree = tree,
                section = MainSection.PROJECTS,
            )
        }
    }

    fun openObject(id: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val (detail, tree, workspaceMembers) = coroutineScope {
                val detailDeferred = async { container.projectRepository.detail(id) }
                val treeDeferred = async {
                    if (_state.value.projectTree.isEmpty()) {
                        runCatching { container.projectRepository.tree() }.getOrDefault(emptyList())
                    } else {
                        _state.value.projectTree
                    }
                }
                val membersDeferred = async {
                    if (_state.value.workspaceMembers.isEmpty()) {
                        runCatching { container.profileRepository.workspaceMembers() }.getOrDefault(emptyList())
                    } else {
                        _state.value.workspaceMembers
                    }
                }
                Triple(detailDeferred.await(), treeDeferred.await(), membersDeferred.await())
            }
            _state.value = _state.value.copy(
                isBusy = false,
                projectTree = tree,
                workspaceMembers = workspaceMembers,
                selectedObject = detail,
                isSavingObjectParticipants = false,
                showDocumentsForSelectedObject = false,
                showDiscussionsForSelectedObject = false,
                showTemplatesForSelectedObject = false,
                selectedDiscussionIdForObject = null,
                section = MainSection.PROJECTS,
            )
        }
    }

    fun closeObject() {
        _state.value = _state.value.copy(
            selectedObject = null,
            isSavingObjectParticipants = false,
            showDocumentsForSelectedObject = false,
            showDiscussionsForSelectedObject = false,
            showTemplatesForSelectedObject = false,
            selectedDiscussionIdForObject = null,
            section = MainSection.PROJECTS,
        )
    }

    fun addSelectedObjectParticipant(userId: String, role: String) {
        val objectId = _state.value.selectedObject?.detail?.id ?: return
        if (userId.isBlank() || role.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isSavingObjectParticipants = true, errorMessage = null)
            container.projectRepository.addParticipant(objectId, userId, role)
            val participants = container.projectRepository.participants(objectId)
            val selected = _state.value.selectedObject
            if (selected != null && selected.detail.id == objectId) {
                _state.value = _state.value.copy(
                    isSavingObjectParticipants = false,
                    selectedObject = selected.copy(participants = participants),
                )
            } else {
                _state.value = _state.value.copy(isSavingObjectParticipants = false)
            }
        }
    }

    fun updateSelectedObjectParticipantRole(userId: String, role: String) {
        val objectId = _state.value.selectedObject?.detail?.id ?: return
        if (userId.isBlank() || role.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isSavingObjectParticipants = true, errorMessage = null)
            container.projectRepository.updateParticipantRole(objectId, userId, role)
            val participants = container.projectRepository.participants(objectId)
            val selected = _state.value.selectedObject
            if (selected != null && selected.detail.id == objectId) {
                _state.value = _state.value.copy(
                    isSavingObjectParticipants = false,
                    selectedObject = selected.copy(participants = participants),
                )
            } else {
                _state.value = _state.value.copy(isSavingObjectParticipants = false)
            }
        }
    }

    fun removeSelectedObjectParticipant(userId: String) {
        val objectId = _state.value.selectedObject?.detail?.id ?: return
        if (userId.isBlank()) return
        launchCatching {
            _state.value = _state.value.copy(isSavingObjectParticipants = true, errorMessage = null)
            container.projectRepository.removeParticipant(objectId, userId)
            val participants = container.projectRepository.participants(objectId)
            val selected = _state.value.selectedObject
            if (selected != null && selected.detail.id == objectId) {
                _state.value = _state.value.copy(
                    isSavingObjectParticipants = false,
                    selectedObject = selected.copy(participants = participants),
                )
            } else {
                _state.value = _state.value.copy(isSavingObjectParticipants = false)
            }
        }
    }

    fun openSelectedObjectDocuments() {
        if (_state.value.selectedObject != null) {
            _state.value = _state.value.copy(
                showDocumentsForSelectedObject = true,
                showDiscussionsForSelectedObject = false,
                showTemplatesForSelectedObject = false,
                selectedDiscussionIdForObject = null,
                section = MainSection.PROJECTS,
            )
        }
    }

    fun closeSelectedObjectDocuments() {
        _state.value = _state.value.copy(showDocumentsForSelectedObject = false, section = MainSection.PROJECTS)
    }

    fun openSelectedObjectDiscussions(initialDiscussionId: String? = null) {
        if (_state.value.selectedObject != null) {
            _state.value = _state.value.copy(
                showDocumentsForSelectedObject = false,
                showDiscussionsForSelectedObject = true,
                showTemplatesForSelectedObject = false,
                selectedDiscussionIdForObject = initialDiscussionId,
                section = MainSection.PROJECTS,
            )
        }
    }

    fun closeSelectedObjectDiscussions() {
        _state.value = _state.value.copy(
            showDiscussionsForSelectedObject = false,
            selectedDiscussionIdForObject = null,
            section = MainSection.PROJECTS,
        )
    }

    fun openSelectedObjectTemplates() {
        if (_state.value.selectedObject != null) {
            _state.value = _state.value.copy(
                showDocumentsForSelectedObject = false,
                showDiscussionsForSelectedObject = false,
                showTemplatesForSelectedObject = true,
                selectedDiscussionIdForObject = null,
                section = MainSection.PROJECTS,
            )
        }
    }

    fun closeSelectedObjectTemplates() {
        _state.value = _state.value.copy(
            showTemplatesForSelectedObject = false,
            section = MainSection.PROJECTS,
        )
    }

    fun clearKnowledgeDeepLink() {
        _state.value = _state.value.copy(
            knowledgeOpenNoteId = null,
            knowledgeOpenArticleId = null,
        )
    }

    fun clearSelectedDiscussionId() {
        _state.value = _state.value.copy(selectedDiscussionIdForObject = null)
    }

    fun updateProfile(firstName: String, lastName: String, email: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val user = container.profileRepository.updateProfile(firstName, lastName, email)
            _state.value = _state.value.copy(isBusy = false, user = user)
        }
    }

    fun inviteWorkspaceMember(email: String, role: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val token = container.profileRepository.inviteMember(email, role)
            val members = container.profileRepository.workspaceMembers()
            _state.value = _state.value.copy(
                isBusy = false,
                workspaceInviteToken = token,
                workspaceMembers = members,
            )
        }
    }

    fun updateWorkspaceMemberRole(userId: String, role: String) {
        launchCatching {
            container.profileRepository.updateWorkspaceMemberRole(userId, role)
            val members = container.profileRepository.workspaceMembers()
            _state.value = _state.value.copy(workspaceMembers = members)
        }
    }

    fun removeWorkspaceMember(userId: String) {
        launchCatching {
            container.profileRepository.removeWorkspaceMember(userId)
            val members = container.profileRepository.workspaceMembers()
            _state.value = _state.value.copy(workspaceMembers = members)
        }
    }

    fun loadProfileMeta() {
        launchCatching {
            val (tg, members) = coroutineScope {
                val tgDeferred = async { container.profileRepository.telegramStatus() }
                val membersDeferred = async { container.profileRepository.workspaceMembers() }
                tgDeferred.await() to membersDeferred.await()
            }
            _state.value = _state.value.copy(
                telegramStatus = tg,
                workspaceMembers = members,
            )
        }
    }

    fun generateTelegramCode() {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val result = container.profileRepository.generateTelegramCode()
            val tgStatus = _state.value.telegramStatus?.copy(botUsername = result.botUsername)
                ?: ru.custle.mobile.core.model.TelegramStatusDto(
                    linked = false,
                    botUsername = result.botUsername,
                )
            _state.value = _state.value.copy(
                isBusy = false,
                telegramStatus = tgStatus,
                telegramLinkCode = result.code,
            )
        }
    }

    fun updateTelegramAutoDelete(minutes: Int) {
        launchCatching {
            container.profileRepository.updateTelegramAutoDelete(minutes)
            val updated = (_state.value.telegramStatus ?: return@launchCatching).copy(autoDeleteMinutes = minutes)
            _state.value = _state.value.copy(telegramStatus = updated)
        }
    }

    fun logout() {
        launchCatching {
            container.authRepository.logout()
            _state.value = CustleUiState(destination = RootDestination.Login)
        }
    }

    private fun bootstrap() {
        launchCatching {
            val session = container.authRepository.currentSession()
            if (session == null) {
                _state.value = CustleUiState(destination = RootDestination.Login)
                return@launchCatching
            }

            val workspaces = container.authRepository.workspaces()
            val user = runCatching { container.authRepository.me() }.getOrNull()
            val activeWorkspaceId = session.activeWorkspaceId

            if (activeWorkspaceId.isNullOrBlank()) {
                _state.value = CustleUiState(
                    destination = RootDestination.WorkspacePicker,
                    user = user,
                    workspaces = workspaces,
                )
                return@launchCatching
            }

            val dashboard = runCatching { container.dashboardRepository.snapshot() }.getOrElse {
                _state.value = CustleUiState(
                    destination = RootDestination.WorkspacePicker,
                    user = user,
                    workspaces = workspaces,
                    errorMessage = "Не удалось загрузить dashboard. Выберите workspace заново.",
                )
                return@launchCatching
            }

            _state.value = CustleUiState(
                destination = RootDestination.Dashboard,
                user = user,
                workspaces = workspaces,
                dashboard = dashboard,
            )
            loadTodos()
            loadProfileMeta()
            loadProjectTree()
            loadReports()
            loadRefTables()
            loadSchema()
            loadDocumentTemplates()
        }
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isBusy = false,
                        isSavingObjectParticipants = false,
                        errorMessage = error.toDebugMessage(),
                    )
                }
        }
    }

    private fun openObjectDiscussion(objectId: String, discussionId: String) {
        launchCatching {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            val detail = container.projectRepository.detail(objectId)
            if (_state.value.projectTree.isEmpty()) {
                val tree = runCatching { container.projectRepository.tree() }.getOrDefault(emptyList())
                _state.value = _state.value.copy(projectTree = tree)
            }
            _state.value = _state.value.copy(
                isBusy = false,
                selectedObject = detail,
                showDocumentsForSelectedObject = false,
                showDiscussionsForSelectedObject = true,
                selectedDiscussionIdForObject = discussionId,
                section = MainSection.PROJECTS,
            )
        }
    }

    private suspend fun completeAuthorizedSession(
        user: ru.custle.mobile.core.model.UserDto?,
        source: String,
    ) {
        val workspaces = runCatching { container.authRepository.workspaces() }
            .getOrElse {
                error(
                    "Authorized session bootstrap failed\n" +
                        it.toDebugMessage("Source: $source\nStep: GET /auth/workspaces"),
                )
            }
        _state.value = _state.value.copy(
            isBusy = false,
            user = user,
            workspaces = workspaces,
            destination = if (workspaces.size == 1) RootDestination.Dashboard else RootDestination.WorkspacePicker,
        )
        if (workspaces.size == 1) {
            selectWorkspace(workspaces.first().id, source = source)
        }
    }
}

class CustleNavigatorViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustleNavigatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustleNavigatorViewModel(container) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
