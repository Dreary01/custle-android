package ru.custle.mobile.core.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.ApiEnvelope
import ru.custle.mobile.core.model.AuthPayload
import ru.custle.mobile.core.model.AncestorDto
import ru.custle.mobile.core.model.ArticleDto
import ru.custle.mobile.core.model.AcceptInvitationRequest
import ru.custle.mobile.core.model.AcceptInvitationResponse
import ru.custle.mobile.core.model.DocumentFileDto
import ru.custle.mobile.core.model.DocumentIndexStatusDto
import ru.custle.mobile.core.model.DocumentInfoDto
import ru.custle.mobile.core.model.CreateTodoRequest
import ru.custle.mobile.core.model.CreateDiscussionMessageRequest
import ru.custle.mobile.core.model.CreateDiscussionRequest
import ru.custle.mobile.core.model.DashboardItemDto
import ru.custle.mobile.core.model.DependencyDto
import ru.custle.mobile.core.model.DiscussionDto
import ru.custle.mobile.core.model.DiscussionMessageDto
import ru.custle.mobile.core.model.InviteWorkspaceMemberRequest
import ru.custle.mobile.core.model.InviteWorkspaceMemberResponse
import ru.custle.mobile.core.model.LoginRequest
import ru.custle.mobile.core.model.MentionDto
import ru.custle.mobile.core.model.MobileOAuthPayload
import ru.custle.mobile.core.model.NewsDto
import ru.custle.mobile.core.model.NoteDto
import ru.custle.mobile.core.model.ObjectDetailDto
import ru.custle.mobile.core.model.ObjectNodeDto
import ru.custle.mobile.core.model.ParticipantDto
import ru.custle.mobile.core.model.PlanDto
import ru.custle.mobile.core.model.ReportDto
import ru.custle.mobile.core.model.RefRecordDto
import ru.custle.mobile.core.model.RefTableDto
import ru.custle.mobile.core.model.ReindexDocumentsResponseDto
import ru.custle.mobile.core.model.ObjectTypeDto
import ru.custle.mobile.core.model.OAuthExchangeRequest
import ru.custle.mobile.core.model.DocTemplateDto
import ru.custle.mobile.core.model.GridStateDto
import ru.custle.mobile.core.model.AdminUserDto
import ru.custle.mobile.core.model.PermissionDto
import ru.custle.mobile.core.model.MarketplaceConfigDto
import ru.custle.mobile.core.model.MarketplaceInstallationDto
import ru.custle.mobile.core.model.SuperadminStatsDto
import ru.custle.mobile.core.model.SuperadminUserDto
import ru.custle.mobile.core.model.SuperadminWorkspaceDto
import ru.custle.mobile.core.model.WidgetCatalogDto
import ru.custle.mobile.core.model.RequisiteDto
import ru.custle.mobile.core.model.RequisiteGroupDto
import ru.custle.mobile.core.model.WidgetLayoutDto
import ru.custle.mobile.core.model.SearchRequest
import ru.custle.mobile.core.model.SearchResultDto
import ru.custle.mobile.core.model.SwitchWorkspaceRequest
import ru.custle.mobile.core.model.TelegramLinkCodeDto
import ru.custle.mobile.core.model.TelegramStatusDto
import ru.custle.mobile.core.model.UpdateDocumentRequest
import ru.custle.mobile.core.model.TodoDto
import ru.custle.mobile.core.model.UpdateTodoRequest
import ru.custle.mobile.core.model.UpdateWorkspaceMemberRoleRequest
import ru.custle.mobile.core.model.UpsertArticleRequest
import ru.custle.mobile.core.model.UpsertNoteRequest
import ru.custle.mobile.core.model.UpdateProfileRequest
import ru.custle.mobile.core.model.UpdateTelegramSettingsRequest
import ru.custle.mobile.core.model.UpdateDiscussionRequest
import ru.custle.mobile.core.model.UpdateDiscussionMessageRequest
import ru.custle.mobile.core.model.UserDto
import ru.custle.mobile.core.model.DeleteDocumentsRequest
import ru.custle.mobile.core.model.InviteObjectParticipantRequest
import ru.custle.mobile.core.model.UploadDocumentResponseDto
import ru.custle.mobile.core.model.WorkspaceDto
import ru.custle.mobile.core.model.WorkspaceMemberDto

interface CustleApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiEnvelope<AuthPayload>

    @POST("auth/oauth/{provider}/exchange")
    suspend fun exchangeOAuthCode(
        @retrofit2.http.Path("provider") provider: String,
        @Body body: OAuthExchangeRequest,
    ): ApiEnvelope<MobileOAuthPayload>

    @GET("auth/workspaces")
    suspend fun listWorkspaces(): ApiEnvelope<List<WorkspaceDto>>

    @POST("auth/switch-workspace")
    suspend fun switchWorkspace(@Body body: SwitchWorkspaceRequest): ApiEnvelope<AuthPayload>

    @POST("invitations/accept")
    suspend fun acceptInvitation(@Body body: AcceptInvitationRequest): ApiEnvelope<AcceptInvitationResponse>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<UserDto>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ApiEnvelope<UserDto>

    @GET("dashboard/requests")
    suspend fun dashboardRequests(): ApiEnvelope<List<DashboardItemDto>>

    @GET("dashboard/directions")
    suspend fun dashboardDirections(): ApiEnvelope<List<DashboardItemDto>>

    @GET("dashboard/events")
    suspend fun dashboardEvents(): ApiEnvelope<List<DashboardItemDto>>

    @GET("news")
    suspend fun news(): ApiEnvelope<List<NewsDto>>

    @GET("notes")
    suspend fun notes(): ApiEnvelope<List<NoteDto>>

    @GET("notes/{id}")
    suspend fun note(@retrofit2.http.Path("id") id: String): ApiEnvelope<NoteDto>

    @POST("notes")
    suspend fun createNote(@Body body: UpsertNoteRequest): ApiEnvelope<NoteDto>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpsertNoteRequest,
    ): ApiEnvelope<NoteDto>

    @DELETE("notes/{id}")
    suspend fun deleteNote(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @GET("articles")
    suspend fun articles(): ApiEnvelope<List<ArticleDto>>

    @GET("articles/{id}")
    suspend fun article(@retrofit2.http.Path("id") id: String): ApiEnvelope<ArticleDto>

    @POST("articles")
    suspend fun createArticle(@Body body: UpsertArticleRequest): ApiEnvelope<ArticleDto>

    @PUT("articles/{id}")
    suspend fun updateArticle(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpsertArticleRequest,
    ): ApiEnvelope<ArticleDto>

    @DELETE("articles/{id}")
    suspend fun deleteArticle(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @GET("objects/tree")
    suspend fun objectTree(): ApiEnvelope<List<ObjectNodeDto>>

    @GET("objects/{id}")
    suspend fun objectDetail(@retrofit2.http.Path("id") id: String): ApiEnvelope<ObjectDetailDto>

    @GET("objects/{id}/ancestors")
    suspend fun objectAncestors(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<AncestorDto>>

    @GET("objects/{id}/participants")
    suspend fun objectParticipants(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<ParticipantDto>>

    @POST("objects/{id}/participants")
    suspend fun addObjectParticipant(
        @retrofit2.http.Path("id") id: String,
        @Body body: InviteObjectParticipantRequest,
    ): retrofit2.Response<Unit>

    @PUT("objects/{id}/participants/{userId}")
    suspend fun updateObjectParticipantRole(
        @retrofit2.http.Path("id") id: String,
        @retrofit2.http.Path("userId") userId: String,
        @Body body: UpdateWorkspaceMemberRoleRequest,
    ): retrofit2.Response<Unit>

    @DELETE("objects/{id}/participants/{userId}")
    suspend fun removeObjectParticipant(
        @retrofit2.http.Path("id") id: String,
        @retrofit2.http.Path("userId") userId: String,
    ): retrofit2.Response<Unit>

    @GET("objects/{id}/plans")
    suspend fun objectPlans(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<PlanDto>>

    @GET("objects/{id}/dependencies")
    suspend fun objectDependencies(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DependencyDto>>

    @GET("objects/{id}/discussions")
    suspend fun objectDiscussions(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DiscussionDto>>

    @POST("objects/{id}/discussions")
    suspend fun createDiscussion(
        @retrofit2.http.Path("id") id: String,
        @Body body: CreateDiscussionRequest,
    ): ApiEnvelope<Map<String, String>>

    @PUT("discussions/{id}")
    suspend fun updateDiscussion(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpdateDiscussionRequest,
    ): ApiEnvelope<Map<String, String>>

    @DELETE("discussions/{id}")
    suspend fun deleteDiscussion(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @GET("discussions/{id}/nested")
    suspend fun nestedDiscussions(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DiscussionDto>>

    @GET("discussions/{id}/messages")
    suspend fun discussionMessages(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DiscussionMessageDto>>

    @POST("discussions/{id}/messages")
    suspend fun createDiscussionMessage(
        @retrofit2.http.Path("id") id: String,
        @Body body: CreateDiscussionMessageRequest,
    ): ApiEnvelope<Map<String, String>>

    @PUT("discussion-messages/{id}")
    suspend fun updateDiscussionMessage(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpdateDiscussionMessageRequest,
    ): ApiEnvelope<Map<String, String>>

    @DELETE("discussion-messages/{id}")
    suspend fun deleteDiscussionMessage(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @GET("objects/{id}/documents/files")
    suspend fun documentFiles(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DocumentFileDto>>

    @GET("objects/{id}/documents/index-status")
    suspend fun documentIndexStatus(@retrofit2.http.Path("id") id: String): ApiEnvelope<List<DocumentIndexStatusDto>>

    @POST("objects/{id}/documents/reindex")
    suspend fun reindexDocuments(@retrofit2.http.Path("id") id: String): ApiEnvelope<ReindexDocumentsResponseDto>

    @GET("objects/{id}/documents/info")
    suspend fun documentInfo(@retrofit2.http.Path("id") id: String): ApiEnvelope<DocumentInfoDto>

    @Multipart
    @POST("objects/{id}/documents/upload")
    suspend fun uploadDocument(
        @retrofit2.http.Path("id") id: String,
        @Part upload: MultipartBody.Part,
        @Query("id") parentPath: String = "/",
    ): ApiEnvelope<UploadDocumentResponseDto>

    @Streaming
    @GET("documents/{docId}/download")
    suspend fun downloadDocument(@retrofit2.http.Path("docId") docId: String): ResponseBody

    @PUT("objects/{id}/documents/files")
    suspend fun updateDocument(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpdateDocumentRequest,
    ): ApiEnvelope<Map<String, String>>

    @DELETE("objects/{id}/documents/files")
    suspend fun deleteDocuments(
        @retrofit2.http.Path("id") id: String,
        @Body body: DeleteDocumentsRequest,
    ): ApiEnvelope<String?>

    @GET("todos")
    suspend fun todos(): ApiEnvelope<List<TodoDto>>

    @POST("todos")
    suspend fun createTodo(@Body body: CreateTodoRequest): ApiEnvelope<TodoDto>

    @PUT("todos/{id}")
    suspend fun updateTodo(
        @retrofit2.http.Path("id") id: String,
        @Body body: UpdateTodoRequest,
    ): ApiEnvelope<TodoDto>

    @PATCH("todos/{id}/toggle")
    suspend fun toggleTodo(@retrofit2.http.Path("id") id: String): ApiEnvelope<TodoDto>

    @DELETE("todos/{id}")
    suspend fun deleteTodo(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @POST("search")
    suspend fun search(@Body body: SearchRequest): ApiEnvelope<List<SearchResultDto>>

    @GET("reports")
    suspend fun reports(): ApiEnvelope<List<ReportDto>>

    @GET("ref-tables")
    suspend fun refTables(): ApiEnvelope<List<RefTableDto>>

    @GET("ref-tables/{id}")
    suspend fun refTable(@retrofit2.http.Path("id") id: String): ApiEnvelope<RefTableDto>

    @GET("ref-tables/{tableId}/records")
    suspend fun refTableRecords(@retrofit2.http.Path("tableId") tableId: String): ApiEnvelope<List<RefRecordDto>>

    @GET("object-types")
    suspend fun objectTypes(): ApiEnvelope<List<ObjectTypeDto>>

    @GET("object-types/{id}")
    suspend fun objectType(@retrofit2.http.Path("id") id: String): ApiEnvelope<ObjectTypeDto>

    @GET("requisites")
    suspend fun requisites(): ApiEnvelope<List<RequisiteDto>>

    @GET("requisite-groups")
    suspend fun requisiteGroups(): ApiEnvelope<List<RequisiteGroupDto>>

    @GET("document-templates")
    suspend fun documentTemplates(
        @Query("type_id") typeId: String? = null,
        @Query("requisite_id") requisiteId: String? = null,
    ): ApiEnvelope<List<DocTemplateDto>>

    @GET("document-templates/{id}")
    suspend fun documentTemplate(@retrofit2.http.Path("id") id: String): ApiEnvelope<DocTemplateDto>

    @Streaming
    @GET("document-templates/{id}/download")
    suspend fun downloadDocumentTemplate(@retrofit2.http.Path("id") id: String): ResponseBody

    @Streaming
    @POST("objects/{id}/generate/{templateId}")
    suspend fun generateDocument(
        @retrofit2.http.Path("id") objectId: String,
        @retrofit2.http.Path("templateId") templateId: String,
        @Query("format") format: String? = null,
    ): ResponseBody

    @GET("widget-layouts")
    suspend fun widgetLayouts(
        @Query("page_type") pageType: String,
        @Query("object_id") objectId: String? = null,
        @Query("type_id") typeId: String? = null,
    ): ApiEnvelope<List<WidgetLayoutDto>>

    @GET("grid-states")
    suspend fun gridState(
        @Query("grid_id") gridId: String,
        @Query("object_id") objectId: String? = null,
        @Query("type_id") typeId: String? = null,
    ): ApiEnvelope<GridStateDto>

    @GET("users")
    suspend fun adminUsers(): ApiEnvelope<List<AdminUserDto>>

    @GET("permissions")
    suspend fun permissions(
        @Query("user_id") userId: String? = null,
        @Query("resource_type") resourceType: String? = null,
        @Query("resource_id") resourceId: String? = null,
    ): ApiEnvelope<List<PermissionDto>>

    @GET("admin/settings")
    suspend fun adminSettings(): ApiEnvelope<Map<String, JsonElement>>

    @GET("modules")
    suspend fun modules(): ApiEnvelope<Map<String, JsonElement>>

    @GET("marketplace")
    suspend fun marketplaceConfigs(): ApiEnvelope<List<MarketplaceConfigDto>>

    @GET("marketplace/installations")
    suspend fun marketplaceInstallations(): ApiEnvelope<List<MarketplaceInstallationDto>>

    @GET("marketplace/installations/{id}/sync-status")
    suspend fun marketplaceSyncStatus(@retrofit2.http.Path("id") id: String): ApiEnvelope<JsonElement>

    @GET("superadmin/stats")
    suspend fun superadminStats(): ApiEnvelope<SuperadminStatsDto>

    @GET("superadmin/workspaces")
    suspend fun superadminWorkspaces(): ApiEnvelope<List<SuperadminWorkspaceDto>>

    @GET("superadmin/users")
    suspend fun superadminUsers(): ApiEnvelope<List<SuperadminUserDto>>

    @GET("superadmin/settings")
    suspend fun superadminSettings(): ApiEnvelope<Map<String, JsonElement>>

    @GET("widget-catalog")
    suspend fun widgetCatalog(): ApiEnvelope<List<WidgetCatalogDto>>

    @POST("widget-catalog/{id}/install")
    suspend fun installWidgetCatalog(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @DELETE("widget-catalog/{id}/uninstall")
    suspend fun uninstallWidgetCatalog(@retrofit2.http.Path("id") id: String): retrofit2.Response<Unit>

    @GET("my/mentions")
    suspend fun myMentions(): ApiEnvelope<List<MentionDto>>

    @PATCH("mentions/{id}/resolve")
    suspend fun resolveMention(@retrofit2.http.Path("id") id: String): ApiEnvelope<Map<String, String>>

    @GET("telegram/status")
    suspend fun telegramStatus(): ApiEnvelope<TelegramStatusDto>

    @GET("workspaces/members")
    suspend fun workspaceMembers(): ApiEnvelope<List<WorkspaceMemberDto>>

    @POST("workspaces/members/invite")
    suspend fun inviteWorkspaceMember(@Body body: InviteWorkspaceMemberRequest): ApiEnvelope<InviteWorkspaceMemberResponse>

    @PUT("workspaces/members/{userId}")
    suspend fun updateWorkspaceMemberRole(
        @retrofit2.http.Path("userId") userId: String,
        @Body body: UpdateWorkspaceMemberRoleRequest,
    ): retrofit2.Response<Unit>

    @DELETE("workspaces/members/{userId}")
    suspend fun removeWorkspaceMember(@retrofit2.http.Path("userId") userId: String): retrofit2.Response<Unit>

    @POST("telegram/link-code")
    suspend fun telegramLinkCode(): ApiEnvelope<TelegramLinkCodeDto>

    @PUT("telegram/settings")
    suspend fun updateTelegramSettings(@Body body: UpdateTelegramSettingsRequest): retrofit2.Response<Unit>
}
