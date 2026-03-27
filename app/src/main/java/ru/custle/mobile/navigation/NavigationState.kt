package ru.custle.mobile.navigation

import androidx.compose.runtime.Stable
import ru.custle.mobile.core.model.DashboardSnapshot
import ru.custle.mobile.core.model.ObjectDetailBundle
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.ObjectTypeDto
import ru.custle.mobile.core.model.DocTemplateDto
import ru.custle.mobile.core.model.RefTableDto
import ru.custle.mobile.core.model.RequisiteDto
import ru.custle.mobile.core.model.RequisiteGroupDto
import ru.custle.mobile.core.model.ReportDto
import ru.custle.mobile.core.model.SearchResultDto
import ru.custle.mobile.core.model.TelegramStatusDto
import ru.custle.mobile.core.model.TodoDto
import ru.custle.mobile.core.model.UserDto
import ru.custle.mobile.core.model.WorkspaceDto
import ru.custle.mobile.core.model.WorkspaceMemberDto

sealed interface RootDestination {
    data object Loading : RootDestination
    data object Login : RootDestination
    data object WorkspacePicker : RootDestination
    data object Dashboard : RootDestination
}

enum class MainSection {
    DASHBOARD,
    INBOX,
    PROJECTS,
    NEWS,
    REPORTS,
    TABLES,
    SCHEMA,
    TEMPLATES,
    LAYOUTS,
    ADMIN,
    MARKETPLACE,
    SUPERADMIN,
    WIDGETS,
    KNOWLEDGE,
    TODOS,
    SEARCH,
    PROFILE,
}

@Stable
data class CustleUiState(
    val destination: RootDestination = RootDestination.Loading,
    val section: MainSection = MainSection.DASHBOARD,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val authWebUrl: String? = null,
    val user: UserDto? = null,
    val workspaces: List<WorkspaceDto> = emptyList(),
    val dashboard: DashboardSnapshot = DashboardSnapshot(),
    val reports: List<ReportDto> = emptyList(),
    val refTables: List<RefTableDto> = emptyList(),
    val objectTypes: List<ObjectTypeDto> = emptyList(),
    val documentTemplates: List<DocTemplateDto> = emptyList(),
    val requisites: List<RequisiteDto> = emptyList(),
    val requisiteGroups: List<RequisiteGroupDto> = emptyList(),
    val todos: List<TodoDto> = emptyList(),
    val searchResults: List<SearchResultDto> = emptyList(),
    val lastSearchQuery: String = "",
    val telegramStatus: TelegramStatusDto? = null,
    val telegramLinkCode: String = "",
    val workspaceInviteToken: String = "",
    val workspaceMembers: List<WorkspaceMemberDto> = emptyList(),
    val projectTree: List<ObjectNodeDto> = emptyList(),
    val selectedObject: ObjectDetailBundle? = null,
    val isSavingObjectParticipants: Boolean = false,
    val showDocumentsForSelectedObject: Boolean = false,
    val showDiscussionsForSelectedObject: Boolean = false,
    val showTemplatesForSelectedObject: Boolean = false,
    val selectedDiscussionIdForObject: String? = null,
    val knowledgeOpenNoteId: String? = null,
    val knowledgeOpenArticleId: String? = null,
)
