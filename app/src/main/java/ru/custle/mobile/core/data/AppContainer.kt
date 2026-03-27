package ru.custle.mobile.core.data

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import ru.custle.mobile.core.network.CustleApi
import ru.custle.mobile.core.network.NetworkModule
import ru.custle.mobile.core.network.SessionStore

class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext

    val sessionStore = SessionStore(appContext)
    private val networkModule = NetworkModule(sessionStore)

    val api: CustleApi = networkModule.api
    val authRepository = AuthRepository(api, sessionStore)
    val dashboardRepository = DashboardRepository(api)
    val todoRepository = TodoRepository(api)
    val searchRepository = SearchRepository(api)
    val profileRepository = ProfileRepository(api)
    val projectRepository = ProjectRepository(api)
    val knowledgeRepository = KnowledgeRepository(api)
    val documentRepository = DocumentRepository(appContext, api)
    val newsRepository = NewsRepository(api)
    val reportRepository = ReportRepository(api)
    val refTableRepository = RefTableRepository(api)
    val schemaRepository = SchemaRepository(api)
    val docTemplateRepository = DocTemplateRepository(appContext, api)
    val layoutInspectorRepository = LayoutInspectorRepository(api)
    val adminRepository = AdminRepository(api)
    val marketplaceRepository = MarketplaceRepository(api)
    val superadminRepository = SuperadminRepository(api)
    val widgetCatalogRepository = WidgetCatalogRepository(api)
    val discussionRepository = DiscussionRepository(api)
    val mentionRepository = MentionRepository(api)
    val appUpdateRepository = AppUpdateRepository(appContext)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
