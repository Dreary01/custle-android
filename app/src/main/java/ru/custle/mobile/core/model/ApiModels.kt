package ru.custle.mobile.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiEnvelope<T>(
    val data: T,
)

@Serializable
data class EmptyApiEnvelope(
    val data: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class OAuthExchangeRequest(
    val code: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
)

@Serializable
data class SearchRequest(
    val query: String,
    @SerialName("top_k")
    val topK: Int = 20,
)

@Serializable
data class UpdateProfileRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
)

@Serializable
data class CreateTodoRequest(
    val title: String,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("object_id")
    val objectId: String? = null,
)

@Serializable
data class UpdateTodoRequest(
    val title: String? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("is_done")
    val isDone: Boolean? = null,
)

@Serializable
data class UpsertNoteRequest(
    val title: String,
    val content: String = "",
    @SerialName("content_json")
    val contentJson: JsonElement? = null,
    val tags: String = "",
    @SerialName("is_private")
    val isPrivate: Boolean? = null,
)

@Serializable
data class UpsertArticleRequest(
    val title: String,
    val content: String = "",
    @SerialName("content_json")
    val contentJson: JsonElement? = null,
    val category: String = "",
    val tags: String = "",
    @SerialName("is_published")
    val isPublished: Boolean? = null,
)

@Serializable
data class UpdateTelegramSettingsRequest(
    @SerialName("auto_delete_minutes")
    val autoDeleteMinutes: Int,
)

@Serializable
data class InviteWorkspaceMemberRequest(
    val email: String,
    val role: String,
)

@Serializable
data class InviteObjectParticipantRequest(
    @SerialName("user_id")
    val userId: String,
    val role: String,
)

@Serializable
data class InviteWorkspaceMemberResponse(
    val token: String,
)

@Serializable
data class UpdateWorkspaceMemberRoleRequest(
    val role: String,
)

@Serializable
data class AcceptInvitationRequest(
    val token: String,
)

@Serializable
data class AcceptInvitationResponse(
    @SerialName("workspace_id")
    val workspaceId: String,
)

@Serializable
data class SwitchWorkspaceRequest(
    @SerialName("workspace_id")
    val workspaceId: String,
)

@Serializable
data class AuthPayload(
    val token: String,
    val user: UserDto? = null,
)

@Serializable
data class MobileOAuthPayload(
    val token: String,
    val user: UserDto? = null,
    val workspace: WorkspaceDto? = null,
    val workspaces: List<WorkspaceDto> = emptyList(),
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
    @SerialName("is_superadmin")
    val isSuperAdmin: Boolean = false,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

@Serializable
data class WorkspaceDto(
    @SerialName("workspace_id")
    val id: String,
    val name: String,
    val role: String? = null,
)

@Serializable
data class WorkspaceMemberDto(
    val id: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    val role: String = "",
    @SerialName("joined_at")
    val joinedAt: String = "",
)

@Serializable
data class DashboardItemDto(
    val id: String,
    val title: String? = null,
    val name: String? = null,
    val type: String? = null,
    val status: String? = null,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
)

@Serializable
data class ObjectNodeDto(
    val id: String,
    @SerialName("type_id")
    val typeId: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    val name: String,
    val code: String? = null,
    val status: String? = null,
    val priority: Int = 0,
    val progress: Int = 0,
    @SerialName("type_name")
    val typeName: String? = null,
    @SerialName("type_kind")
    val typeKind: String? = null,
    @SerialName("type_color")
    val typeColor: String? = null,
    @SerialName("type_icon")
    val typeIcon: String? = null,
    @SerialName("plan_start_date")
    val planStartDate: String? = null,
    @SerialName("plan_end_date")
    val planEndDate: String? = null,
    @SerialName("plan_duration_days")
    val planDurationDays: Int? = null,
    @SerialName("assignee_name")
    val assigneeName: String? = null,
    val children: List<ObjectNodeDto> = emptyList(),
)

@Serializable
data class PlanDto(
    val id: String? = null,
    @SerialName("object_id")
    val objectId: String,
    @SerialName("plan_type")
    val planType: String,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    @SerialName("duration_days")
    val durationDays: Int? = null,
    @SerialName("effort_hours")
    val effortHours: Double? = null,
)

@Serializable
data class ParticipantDto(
    @SerialName("object_id")
    val objectId: String,
    @SerialName("user_id")
    val userId: String,
    val role: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("user_email")
    val userEmail: String,
)

@Serializable
data class DependencyDto(
    val id: String,
    @SerialName("predecessor_id")
    val predecessorId: String,
    @SerialName("successor_id")
    val successorId: String,
    val type: String,
    @SerialName("lag_days")
    val lagDays: Int = 0,
)

@Serializable
data class AncestorDto(
    val id: String,
    val name: String,
)

@Serializable
data class ObjectDetailDto(
    val id: String,
    @SerialName("type_id")
    val typeId: String,
    @SerialName("parent_id")
    val parentId: String? = null,
    val name: String,
    val code: String? = null,
    val description: String? = null,
    val status: String,
    val priority: Int = 0,
    val progress: Int = 0,
    @SerialName("type_name")
    val typeName: String? = null,
    @SerialName("type_kind")
    val typeKind: String? = null,
    @SerialName("owner_name")
    val ownerName: String? = null,
    @SerialName("assignee_name")
    val assigneeName: String? = null,
    @SerialName("actual_start_date")
    val actualStartDate: String? = null,
    @SerialName("actual_end_date")
    val actualEndDate: String? = null,
    val plans: List<PlanDto> = emptyList(),
    val children: List<ObjectNodeDto> = emptyList(),
)

@Serializable
data class TodoDto(
    val id: String,
    val title: String,
    @SerialName("is_done")
    val isDone: Boolean = false,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("object_name")
    val objectName: String? = null,
)

@Serializable
data class SearchResultDto(
    val type: String,
    val id: String,
    val title: String,
    val snippet: String = "",
    val score: Double = 0.0,
    val source: String = "",
)

@Serializable
data class MentionDto(
    val id: String,
    @SerialName("is_request")
    val isRequest: Boolean = false,
    @SerialName("is_resolved")
    val isResolved: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("message_preview")
    val messagePreview: String = "",
    @SerialName("discussion_title")
    val discussionTitle: String = "",
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("object_name")
    val objectName: String? = null,
    @SerialName("author_name")
    val authorName: String = "",
)

@Serializable
data class NoteDto(
    val id: String,
    val title: String,
    val content: String = "",
    @SerialName("content_json")
    val contentJson: JsonElement? = null,
    val tags: String = "",
    @SerialName("is_private")
    val isPrivate: Boolean = true,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
)

@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val content: String = "",
    @SerialName("content_json")
    val contentJson: JsonElement? = null,
    val category: String = "",
    val tags: String = "",
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
)

@Serializable
data class TelegramStatusDto(
    val linked: Boolean,
    @SerialName("telegram_username")
    val telegramUsername: String? = null,
    @SerialName("bot_username")
    val botUsername: String = "",
    @SerialName("auto_delete_minutes")
    val autoDeleteMinutes: Int? = null,
)

@Serializable
data class TelegramLinkCodeDto(
    val code: String,
    @SerialName("bot_username")
    val botUsername: String,
)

data class Session(
    val token: String,
    val activeWorkspaceId: String? = null,
)

data class DashboardSnapshot(
    val requests: List<DashboardItemDto> = emptyList(),
    val directions: List<DashboardItemDto> = emptyList(),
    val events: List<DashboardItemDto> = emptyList(),
)

@Serializable
data class AppUpdateMetadata(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val versionedApkUrl: String? = null,
    val publishedAt: String? = null,
)

data class ObjectDetailBundle(
    val detail: ObjectDetailDto,
    val ancestors: List<AncestorDto> = emptyList(),
    val participants: List<ParticipantDto> = emptyList(),
    val plans: List<PlanDto> = emptyList(),
    val dependencies: List<DependencyDto> = emptyList(),
)

@Serializable
data class DocumentFileDto(
    val id: String,
    val size: Long = 0,
    val date: String = "",
    val type: String = "",
    val parent: String? = null,
    @SerialName("object_name")
    val objectName: String? = null,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("doc_id")
    val docId: String,
)

@Serializable
data class DocumentIndexStatusDto(
    @SerialName("doc_id")
    val docId: String,
    val indexed: Boolean = false,
)

@Serializable
data class DocumentInfoDto(
    val features: DocumentInfoFeaturesDto = DocumentInfoFeaturesDto(),
)

@Serializable
data class DocumentInfoFeaturesDto(
    val upload: Boolean = false,
    val download: Boolean = false,
    val rename: Boolean = false,
    val delete: Boolean = false,
    val copy: Boolean = false,
    val move: Boolean = false,
)

@Serializable
data class UploadDocumentResponseDto(
    val id: String,
    @SerialName("doc_id")
    val docId: String,
)

@Serializable
data class UpdateDocumentRequest(
    val operation: String,
    val name: String,
    @SerialName("doc_id")
    val docId: String,
)

@Serializable
data class DeleteDocumentsRequest(
    val ids: List<String>,
)

@Serializable
data class ReindexDocumentsResponseDto(
    val queued: Int = 0,
)

@Serializable
data class NewsDto(
    val id: String,
    val title: String,
    val body: String = "",
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("created_by")
    val createdBy: String = "",
    @SerialName("author_name")
    val authorName: String = "",
)

@Serializable
data class ReportDto(
    val id: String,
    val name: String,
    val config: JsonElement? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
)

@Serializable
data class RequisiteDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val type: String = "",
    val config: JsonElement? = null,
    @SerialName("is_unique")
    val isUnique: Boolean = false,
)

@Serializable
data class RefTableColumnDto(
    val id: String,
    @SerialName("table_id")
    val tableId: String,
    @SerialName("requisite_id")
    val requisiteId: String,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("is_visible")
    val isVisible: Boolean = true,
    val aggregation: String? = null,
    val requisite: RequisiteDto? = null,
)

@Serializable
data class RefTableDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val structure: String = "",
    @SerialName("input_mode")
    val inputMode: String = "",
    @SerialName("show_on_main_page")
    val showOnMainPage: Boolean = false,
    @SerialName("use_date")
    val useDate: Boolean = false,
    @SerialName("date_auto_fill")
    val dateAutoFill: Boolean = false,
    @SerialName("has_approval")
    val hasApproval: Boolean = false,
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("object_name")
    val objectName: String? = null,
    @SerialName("is_system")
    val isSystem: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    val columns: List<RefTableColumnDto> = emptyList(),
)

@Serializable
data class RefRecordDto(
    val id: String,
    @SerialName("table_id")
    val tableId: String,
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("parent_record_id")
    val parentRecordId: String? = null,
    val data: JsonElement? = null,
    @SerialName("record_date")
    val recordDate: String? = null,
    @SerialName("is_approved")
    val isApproved: Boolean = false,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("created_by")
    val createdBy: String? = null,
)

@Serializable
data class RequisiteGroupDto(
    val id: String,
    val name: String,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
)

@Serializable
data class ObjectTypeDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val kind: String = "",
    val icon: String? = null,
    val color: String? = null,
    @SerialName("can_be_root")
    val canBeRoot: Boolean = false,
    @SerialName("default_duration_days")
    val defaultDurationDays: Int? = null,
    @SerialName("auto_fill_effort")
    val autoFillEffort: Boolean = false,
    @SerialName("add_to_calendar")
    val addToCalendar: Boolean = false,
    @SerialName("check_uniqueness")
    val checkUniqueness: Boolean = false,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("child_type_ids")
    val childTypeIds: List<String> = emptyList(),
    @SerialName("parent_type_ids")
    val parentTypeIds: List<String> = emptyList(),
    val requisites: List<ObjectTypeRequisiteDto> = emptyList(),
    @SerialName("ref_table_ids")
    val refTableIds: List<String> = emptyList(),
)

@Serializable
data class ObjectTypeRequisiteDto(
    val id: String,
    @SerialName("object_type_id")
    val objectTypeId: String,
    @SerialName("requisite_id")
    val requisiteId: String,
    @SerialName("is_required")
    val isRequired: Boolean = false,
    @SerialName("is_visible")
    val isVisible: Boolean = true,
    @SerialName("is_lockable")
    val isLockable: Boolean = false,
    @SerialName("auto_sum")
    val autoSum: Boolean = false,
    @SerialName("auto_avg")
    val autoAvg: Boolean = false,
    @SerialName("inherit_to_children")
    val inheritToChildren: Boolean = false,
    @SerialName("is_olap_dimension")
    val isOlapDimension: Boolean = false,
    @SerialName("sort_order")
    val sortOrder: Int = 0,
    @SerialName("is_conditional")
    val isConditional: Boolean = false,
    @SerialName("condition_requisite_id")
    val conditionRequisiteId: String? = null,
    @SerialName("condition_value")
    val conditionValue: String? = null,
    val requisite: RequisiteDto? = null,
)

@Serializable
data class DocTemplateDto(
    val id: String,
    val name: String,
    @SerialName("object_type_id")
    val objectTypeId: String? = null,
    @SerialName("object_type_name")
    val objectTypeName: String? = null,
    @SerialName("requisite_id")
    val requisiteId: String? = null,
    @SerialName("carbone_template_id")
    val carboneTemplateId: String? = null,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
)

@Serializable
data class WidgetLayoutDto(
    val id: String,
    val scope: String,
    @SerialName("page_type")
    val pageType: String,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("object_id")
    val objectId: String? = null,
    @SerialName("type_id")
    val typeId: String? = null,
    val layout: JsonElement? = null,
)

@Serializable
data class GridStateDto(
    val state: JsonElement? = null,
    val scope: String? = null,
)

@Serializable
data class AdminUserDto(
    val id: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
)

@Serializable
data class PermissionDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("resource_type")
    val resourceType: String,
    @SerialName("resource_id")
    val resourceId: String,
    val actions: Int = 0,
    val recursive: Boolean = false,
    @SerialName("granted_by")
    val grantedBy: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("resource_name")
    val resourceName: String? = null,
    @SerialName("user_name")
    val userName: String? = null,
)

@Serializable
data class MarketplaceConfigDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val category: String? = null,
    val schema: JsonElement? = null,
    @SerialName("credential_fields")
    val credentialFields: JsonElement? = null,
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    val installed: Boolean = false,
)

@Serializable
data class MarketplaceInstallationDto(
    val id: String,
    @SerialName("config_id")
    val configId: String,
    @SerialName("config_name")
    val configName: String,
    @SerialName("config_icon")
    val configIcon: String? = null,
    val credentials: JsonElement? = null,
    @SerialName("installed_at")
    val installedAt: String = "",
    @SerialName("credential_fields")
    val credentialFields: JsonElement? = null,
)

@Serializable
data class SuperadminStatsDto(
    @SerialName("total_workspaces")
    val totalWorkspaces: Int = 0,
    @SerialName("active_workspaces")
    val activeWorkspaces: Int = 0,
    @SerialName("total_users")
    val totalUsers: Int = 0,
    @SerialName("total_objects")
    val totalObjects: Int = 0,
    @SerialName("new_workspaces_7d")
    val newWorkspaces7d: Int = 0,
    @SerialName("new_users_7d")
    val newUsers7d: Int = 0,
    @SerialName("total_ref_tables")
    val totalRefTables: Int = 0,
    @SerialName("total_ref_records")
    val totalRefRecords: Int = 0,
)

@Serializable
data class SuperadminWorkspaceDto(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("owner_email")
    val ownerEmail: String,
    @SerialName("is_system")
    val isSystem: Boolean = false,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("member_count")
    val memberCount: Int = 0,
    @SerialName("object_count")
    val objectCount: Int = 0,
    @SerialName("doc_count")
    val docCount: Int = 0,
    @SerialName("doc_size_bytes")
    val docSizeBytes: Long = 0,
    @SerialName("created_at")
    val createdAt: String = "",
)

@Serializable
data class SuperadminUserDto(
    val id: String,
    val email: String,
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_superadmin")
    val isSuperAdmin: Boolean = false,
    @SerialName("workspace_count")
    val workspaceCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String = "",
)

@Serializable
data class WidgetCatalogDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    @SerialName("preview_image")
    val previewImage: String? = null,
    val config: JsonElement? = null,
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("created_by")
    val createdBy: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    val installed: Boolean = false,
)

@Serializable
data class DiscussionDto(
    val id: String,
    val title: String,
    val kind: String = "topic",
    @SerialName("is_closed")
    val isClosed: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("message_count")
    val messageCount: Int = 0,
    @SerialName("last_message_at")
    val lastMessageAt: String? = null,
    @SerialName("unread_count")
    val unreadCount: Int = 0,
)

@Serializable
data class DiscussionMessageDto(
    val id: String,
    val content: String,
    @SerialName("parent_message_id")
    val parentMessageId: String? = null,
    @SerialName("is_edited")
    val isEdited: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("author_name")
    val authorName: String = "",
    @SerialName("created_by")
    val createdBy: String = "",
    val mentions: List<DiscussionMentionDto> = emptyList(),
    @SerialName("nested_disc_id")
    val nestedDiscId: String? = null,
    @SerialName("nested_disc_title")
    val nestedDiscTitle: String? = null,
    @SerialName("nested_disc_msg_count")
    val nestedDiscMsgCount: Int = 0,
)

@Serializable
data class DiscussionMentionDto(
    val id: String,
    @SerialName("mentioned_user_id")
    val mentionedUserId: String,
    @SerialName("user_name")
    val userName: String = "",
    @SerialName("is_request")
    val isRequest: Boolean = false,
    @SerialName("is_resolved")
    val isResolved: Boolean = false,
)

@Serializable
data class CreateDiscussionRequest(
    val title: String,
    @SerialName("parent_discussion_id")
    val parentDiscussionId: String? = null,
    @SerialName("parent_message_id")
    val parentMessageId: String? = null,
)

@Serializable
data class UpdateDiscussionRequest(
    val title: String? = null,
    @SerialName("is_closed")
    val isClosed: Boolean? = null,
)

@Serializable
data class CreateDiscussionMessageRequest(
    val content: String,
    @SerialName("mentioned_user_ids")
    val mentionedUserIds: List<String> = emptyList(),
    @SerialName("is_request")
    val isRequest: Boolean = false,
)

@Serializable
data class UpdateDiscussionMessageRequest(
    val content: String,
)
