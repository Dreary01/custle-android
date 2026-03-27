package ru.custle.mobile.navigation

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ru.custle.mobile.feature.admin.AdminRoute
import ru.custle.mobile.feature.auth.LoginScreen
import ru.custle.mobile.feature.dashboard.DashboardScreen
import ru.custle.mobile.feature.discussions.DiscussionsRoute
import ru.custle.mobile.feature.doctemplates.DocTemplatesRoute
import ru.custle.mobile.feature.documents.DocumentsRoute
import ru.custle.mobile.feature.knowledge.KnowledgeBaseRoute
import ru.custle.mobile.feature.layouts.LayoutsRoute
import ru.custle.mobile.feature.marketplace.MarketplaceRoute
import ru.custle.mobile.feature.mentions.MentionsRoute
import ru.custle.mobile.feature.news.NewsRoute
import ru.custle.mobile.feature.profile.ProfileScreen
import ru.custle.mobile.feature.projects.ObjectDetailScreen
import ru.custle.mobile.feature.projects.ProjectsScreen
import ru.custle.mobile.feature.reftables.RefTablesRoute
import ru.custle.mobile.feature.reports.ReportsRoute
import ru.custle.mobile.feature.schema.SchemaRoute
import ru.custle.mobile.feature.search.SearchScreen
import ru.custle.mobile.feature.superadmin.SuperadminRoute
import ru.custle.mobile.feature.todos.TodosScreen
import ru.custle.mobile.feature.widgets.WidgetStoreRoute
import ru.custle.mobile.feature.workspace.WorkspacePickerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustleNavHost(
    state: CustleUiState,
    onLogin: (String, String) -> Unit,
    onLoginWithYandex: () -> Unit,
    onCancelAuthWebView: () -> Unit,
    onAuthWebNavigation: (String) -> Boolean,
    onConsumeAuthLaunch: () -> Unit,
    onSelectWorkspace: (String) -> Unit,
    onAcceptWorkspaceInvitation: (String) -> Unit,
    onRefresh: () -> Unit,
    onSelectSection: (MainSection) -> Unit,
    onCreateTodo: (String, String?, String?) -> Unit,
    onUpdateTodo: (String, String, String?, String?, Boolean?) -> Unit,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenSearchResult: (String, String) -> Unit,
    onClearKnowledgeDeepLink: () -> Unit,
    onSaveProfile: (String, String, String) -> Unit,
    onInviteWorkspaceMember: (String, String) -> Unit,
    onUpdateWorkspaceMemberRole: (String, String) -> Unit,
    onRemoveWorkspaceMember: (String) -> Unit,
    onGenerateTelegramCode: () -> Unit,
    onUpdateTelegramAutoDelete: (Int) -> Unit,
    onOpenObject: (String) -> Unit,
    onOpenSelectedObjectDocuments: () -> Unit,
    onOpenSelectedObjectDiscussions: (String?) -> Unit,
    onOpenSelectedObjectTemplates: () -> Unit,
    onAddSelectedObjectParticipant: (String, String) -> Unit,
    onUpdateSelectedObjectParticipantRole: (String, String) -> Unit,
    onRemoveSelectedObjectParticipant: (String) -> Unit,
    onCloseObject: () -> Unit,
    onCloseSelectedObjectDocuments: () -> Unit,
    onCloseSelectedObjectDiscussions: () -> Unit,
    onCloseSelectedObjectTemplates: () -> Unit,
    onClearSelectedDiscussionId: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    var showSectionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.authWebUrl) {
        val url = state.authWebUrl ?: return@LaunchedEffect
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
        onConsumeAuthLaunch()
    }

    if (showSectionSheet && state.destination == RootDestination.Dashboard) {
        SectionSheet(
            state = state,
            onDismiss = { showSectionSheet = false },
            onSelectSection = {
                showSectionSheet = false
                onSelectSection(it)
            },
        )
    }

    Scaffold(
        topBar = {
            if (state.destination == RootDestination.Dashboard) {
                CenterAlignedTopAppBar(
                    title = {
                        if (state.section == MainSection.DASHBOARD) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Custle", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Сегодня",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(sectionTitle(state), fontWeight = FontWeight.SemiBold)
                                Text(
                                    sectionSubtitle(state),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showSectionSheet = true }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Sections")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (state.destination == RootDestination.Dashboard) {
                NavigationBar {
                    primarySections.forEach { item ->
                        NavigationBarItem(
                            selected = state.section == item.section,
                            onClick = { onSelectSection(item.section) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when (state.destination) {
                RootDestination.Loading -> CircularProgressIndicator()
                RootDestination.Login -> LoginScreen(
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    onLogin = onLogin,
                    onLoginWithYandex = onLoginWithYandex,
                )

                RootDestination.WorkspacePicker -> WorkspacePickerScreen(
                    workspaces = state.workspaces,
                    isBusy = state.isBusy,
                    errorMessage = state.errorMessage,
                    onSelect = onSelectWorkspace,
                    onAcceptInvitation = onAcceptWorkspaceInvitation,
                )

                RootDestination.Dashboard -> when (state.section) {
                    MainSection.DASHBOARD -> DashboardScreen(
                        state = state,
                        onRefresh = onRefresh,
                        onOpenObject = onOpenObject,
                        onOpenSection = onSelectSection,
                        onLogout = onLogout,
                    )

                    MainSection.INBOX -> MentionsRoute(
                        onOpenObject = onOpenObject,
                    )

                    MainSection.PROJECTS -> if (state.selectedObject != null) {
                        if (state.showDiscussionsForSelectedObject) {
                            DiscussionsRoute(
                                objectId = state.selectedObject.detail.id,
                                objectName = state.selectedObject.detail.name,
                                initialDiscussionId = state.selectedDiscussionIdForObject,
                                onInitialDiscussionConsumed = onClearSelectedDiscussionId,
                                onBack = onCloseSelectedObjectDiscussions,
                            )
                        } else if (state.showTemplatesForSelectedObject) {
                            DocTemplatesRoute(
                                objectId = state.selectedObject.detail.id,
                                objectName = state.selectedObject.detail.name,
                                objectTypeId = state.selectedObject.detail.typeId,
                                onBack = onCloseSelectedObjectTemplates,
                            )
                        } else if (state.showDocumentsForSelectedObject) {
                            DocumentsRoute(
                                objectId = state.selectedObject.detail.id,
                                objectName = state.selectedObject.detail.name,
                                onBack = onCloseSelectedObjectDocuments,
                            )
                        } else {
                            ObjectDetailScreen(
                                bundle = state.selectedObject,
                                workspaceMembers = state.workspaceMembers,
                                isSavingParticipants = state.isSavingObjectParticipants,
                                onOpenDocuments = onOpenSelectedObjectDocuments,
                                onOpenDiscussions = { onOpenSelectedObjectDiscussions(null) },
                                onOpenTemplates = onOpenSelectedObjectTemplates,
                                onCreateTodo = onCreateTodo,
                                onAddParticipant = onAddSelectedObjectParticipant,
                                onUpdateParticipantRole = onUpdateSelectedObjectParticipantRole,
                                onRemoveParticipant = onRemoveSelectedObjectParticipant,
                                onOpenChildObject = onOpenObject,
                                onBack = onCloseObject,
                            )
                        }
                    } else {
                        ProjectsScreen(
                            tree = state.projectTree,
                            onOpenObject = onOpenObject,
                        )
                    }

                    MainSection.KNOWLEDGE -> KnowledgeBaseRoute(
                        initialNoteId = state.knowledgeOpenNoteId,
                        initialArticleId = state.knowledgeOpenArticleId,
                        onInitialNavigationConsumed = onClearKnowledgeDeepLink,
                    )

                    MainSection.NEWS -> NewsRoute()
                    MainSection.REPORTS -> ReportsRoute()
                    MainSection.TABLES -> RefTablesRoute()
                    MainSection.SCHEMA -> SchemaRoute()
                    MainSection.TEMPLATES -> DocTemplatesRoute()
                    MainSection.LAYOUTS -> LayoutsRoute()
                    MainSection.ADMIN -> AdminRoute()
                    MainSection.MARKETPLACE -> MarketplaceRoute()
                    MainSection.SUPERADMIN -> SuperadminRoute()
                    MainSection.WIDGETS -> WidgetStoreRoute()

                    MainSection.TODOS -> TodosScreen(
                        todos = state.todos,
                        projectTree = state.projectTree,
                        isBusy = state.isBusy,
                        onCreate = onCreateTodo,
                        onUpdate = onUpdateTodo,
                        onToggle = onToggleTodo,
                        onDelete = onDeleteTodo,
                        onOpenObject = onOpenObject,
                    )

                    MainSection.SEARCH -> SearchScreen(
                        lastQuery = state.lastSearchQuery,
                        results = state.searchResults,
                        onSearch = onSearch,
                        onOpenResult = onOpenSearchResult,
                    )

                    MainSection.PROFILE -> ProfileScreen(
                        user = state.user,
                        telegramStatus = state.telegramStatus,
                        telegramCode = state.telegramLinkCode,
                        workspaceMembers = state.workspaceMembers,
                        workspaceInviteToken = state.workspaceInviteToken,
                        onSave = onSaveProfile,
                        onInviteWorkspaceMember = onInviteWorkspaceMember,
                        onUpdateWorkspaceMemberRole = onUpdateWorkspaceMemberRole,
                        onRemoveWorkspaceMember = onRemoveWorkspaceMember,
                        onGenerateTelegramCode = onGenerateTelegramCode,
                        onUpdateTelegramAutoDelete = onUpdateTelegramAutoDelete,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionSheet(
    state: CustleUiState,
    onDismiss: () -> Unit,
    onSelectSection: (MainSection) -> Unit,
) {
    val userSections = listOf(
        SectionItem(MainSection.DASHBOARD, "Главная", "Сводка и быстрый вход", Icons.Outlined.Home),
        SectionItem(MainSection.PROJECTS, "Проекты", "Дерево и карточки объектов", Icons.Outlined.FolderCopy),
        SectionItem(MainSection.INBOX, "Входящие", "Упоминания и запросы", Icons.Outlined.Notifications),
        SectionItem(MainSection.TODOS, "Задачи", "Личные todo с привязкой к объектам", Icons.Outlined.CheckCircle),
        SectionItem(MainSection.SEARCH, "Поиск", "Глобальный поиск по данным", Icons.Outlined.Search),
        SectionItem(MainSection.KNOWLEDGE, "Знания", "Заметки и статьи", Icons.Outlined.Book),
        SectionItem(MainSection.NEWS, "Новости", "Лента публикаций", Icons.AutoMirrored.Outlined.Article),
        SectionItem(MainSection.TEMPLATES, "Шаблоны", "Шаблоны и генерация документов", Icons.Outlined.Description),
        SectionItem(MainSection.REPORTS, "Отчёты", "Read-only отчёты", Icons.Outlined.DateRange),
    )
    val workspaceSections = listOf(
        SectionItem(MainSection.TABLES, "Таблицы", "Справочники и записи", Icons.AutoMirrored.Outlined.ViewList),
        SectionItem(MainSection.SCHEMA, "Схема", "Типы объектов и реквизиты", Icons.Outlined.Tune),
        SectionItem(MainSection.LAYOUTS, "Layouts", "Widget layouts и grid states", Icons.Outlined.DashboardCustomize),
        SectionItem(MainSection.WIDGETS, "Widgets", "Каталог и установка виджетов", Icons.Outlined.Widgets),
        SectionItem(MainSection.MARKETPLACE, "Marketplace", "Установки и синхронизация", Icons.Outlined.Storefront),
    )
    val adminSections = buildList {
        if (state.user?.isAdmin == true) {
            add(SectionItem(MainSection.ADMIN, "Admin", "Users, permissions, settings", Icons.Outlined.AdminPanelSettings))
        }
        if (state.user?.isSuperAdmin == true) {
            add(SectionItem(MainSection.SUPERADMIN, "Superadmin", "Платформенные инструменты", Icons.Outlined.Security))
        }
        add(SectionItem(MainSection.PROFILE, "Профиль", "Аккаунт, Telegram и обновления", Icons.Outlined.AccountCircle))
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { SheetHeader("Основное") }
            items(userSections, key = { it.section.name }) { item ->
                SheetItem(item, onSelectSection)
            }
            item { SheetHeader("Пространство") }
            items(workspaceSections, key = { it.section.name }) { item ->
                SheetItem(item, onSelectSection)
            }
            item { SheetHeader("Аккаунт и доступ") }
            items(adminSections, key = { it.section.name }) { item ->
                SheetItem(item, onSelectSection)
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SheetItem(
    item: SectionItem,
    onSelectSection: (MainSection) -> Unit,
) {
    ListItem(
        headlineContent = { Text(item.label) },
        supportingContent = { Text(item.hint) },
        leadingContent = { Icon(item.icon, contentDescription = null) },
        modifier = Modifier
            .clickable { onSelectSection(item.section) }
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 4.dp),
        overlineContent = null,
        trailingContent = null,
    )
}

private data class SectionItem(
    val section: MainSection,
    val label: String,
    val hint: String,
    val icon: ImageVector,
)

private data class PrimaryNavItem(
    val section: MainSection,
    val label: String,
    val icon: ImageVector,
)

private val primarySections = listOf(
    PrimaryNavItem(MainSection.DASHBOARD, "Главная", Icons.Outlined.Home),
    PrimaryNavItem(MainSection.PROJECTS, "Проекты", Icons.Outlined.FolderCopy),
    PrimaryNavItem(MainSection.INBOX, "Входящие", Icons.Outlined.Notifications),
    PrimaryNavItem(MainSection.SEARCH, "Поиск", Icons.Outlined.Search),
    PrimaryNavItem(MainSection.PROFILE, "Профиль", Icons.Outlined.AccountCircle),
)

private fun sectionTitle(state: CustleUiState): String = when {
    state.section == MainSection.PROJECTS && state.selectedObject != null -> state.selectedObject.detail.name
    else -> when (state.section) {
        MainSection.DASHBOARD -> "Custle"
        MainSection.INBOX -> "Inbox"
        MainSection.PROJECTS -> "Projects"
        MainSection.NEWS -> "News"
        MainSection.REPORTS -> "Reports"
        MainSection.TABLES -> "Tables"
        MainSection.SCHEMA -> "Schema"
        MainSection.TEMPLATES -> "Templates"
        MainSection.LAYOUTS -> "Layouts"
        MainSection.ADMIN -> "Admin"
        MainSection.MARKETPLACE -> "Marketplace"
        MainSection.SUPERADMIN -> "Superadmin"
        MainSection.WIDGETS -> "Widgets"
        MainSection.KNOWLEDGE -> "Knowledge"
        MainSection.TODOS -> "Tasks"
        MainSection.SEARCH -> "Search"
        MainSection.PROFILE -> "Profile"
    }
}

private fun sectionSubtitle(state: CustleUiState): String = when {
    state.section == MainSection.PROJECTS && state.selectedObject != null -> "Карточка объекта"
    else -> when (state.section) {
        MainSection.DASHBOARD -> "Рабочий центр"
        MainSection.INBOX -> "Упоминания и запросы"
        MainSection.PROJECTS -> "Объекты и дерево"
        MainSection.NEWS -> "Публикации команды"
        MainSection.REPORTS -> "Read-only аналитика"
        MainSection.TABLES -> "Справочники"
        MainSection.SCHEMA -> "Типы и реквизиты"
        MainSection.TEMPLATES -> "Документы и генерация"
        MainSection.LAYOUTS -> "Технические layouts"
        MainSection.ADMIN -> "Настройки workspace"
        MainSection.MARKETPLACE -> "Интеграции"
        MainSection.SUPERADMIN -> "Платформенный контур"
        MainSection.WIDGETS -> "Каталог виджетов"
        MainSection.KNOWLEDGE -> "Заметки и статьи"
        MainSection.TODOS -> "Личный план"
        MainSection.SEARCH -> "Глобальный поиск"
        MainSection.PROFILE -> "Аккаунт и доступ"
    }
}
