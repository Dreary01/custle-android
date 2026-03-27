package ru.custle.mobile.core.data

import android.net.Uri
import kotlinx.coroutines.flow.first
import ru.custle.mobile.BuildConfig
import ru.custle.mobile.core.model.AcceptInvitationRequest
import ru.custle.mobile.core.model.MobileOAuthPayload
import ru.custle.mobile.core.model.OAuthExchangeRequest
import ru.custle.mobile.core.model.Session
import ru.custle.mobile.core.model.UserDto
import ru.custle.mobile.core.model.WorkspaceDto
import ru.custle.mobile.core.network.CustleApi
import ru.custle.mobile.core.network.SessionStore

class AuthRepository(
    private val api: CustleApi,
    private val sessionStore: SessionStore,
) {
    private val webBaseUrl = BuildConfig.DEFAULT_WEB_URL.ensureTrailingSlash()
    private val mobileRedirectUri = "custle://oauth-callback"

    suspend fun currentSession(): Session? = sessionStore.sessionFlow.first()

    suspend fun login(email: String, password: String): UserDto? {
        val payload = api.login(ru.custle.mobile.core.model.LoginRequest(email, password)).data
        sessionStore.saveToken(payload.token)
        return payload.user
    }

    suspend fun workspaces(): List<WorkspaceDto> = api.listWorkspaces().data

    suspend fun switchWorkspace(workspaceId: String) {
        api.switchWorkspace(ru.custle.mobile.core.model.SwitchWorkspaceRequest(workspaceId))
        sessionStore.saveWorkspace(workspaceId)
    }

    suspend fun acceptInvitation(token: String): String {
        return api.acceptInvitation(AcceptInvitationRequest(token)).data.workspaceId
    }

    suspend fun me(): UserDto = api.me().data

    suspend fun completeOAuth(token: String): UserDto? {
        sessionStore.saveToken(token)
        return runCatching { me() }.getOrNull()
    }

    suspend fun completeMobileOAuth(code: String): MobileOAuthPayload {
        val payload = api.exchangeOAuthCode(
            provider = "yandex",
            body = OAuthExchangeRequest(
                code = code,
                redirectUri = mobileRedirectUri,
            ),
        ).data
        sessionStore.saveToken(payload.token)
        return payload
    }

    fun yandexOAuthUrl(): String =
        "${webBaseUrl}api/auth/oauth/yandex?platform=mobile&redirect_uri=${Uri.encode(mobileRedirectUri)}"

    fun isMobileOAuthCallback(uri: Uri): Boolean {
        val expected = Uri.parse(mobileRedirectUri)
        return uri.scheme == expected.scheme &&
            uri.host == expected.host &&
            uri.path == expected.path
    }

    fun isOAuthWebError(uri: Uri): Boolean {
        val expected = Uri.parse("${webBaseUrl}login")
        return uri.scheme == expected.scheme &&
            uri.host == expected.host &&
            uri.port == expected.port &&
            uri.path == expected.path &&
            !uri.getQueryParameter("error").isNullOrBlank()
    }

    fun extractOAuthCode(uri: Uri): String {
        return uri.getQueryParameter("code")
            ?.takeIf { it.isNotBlank() }
            ?: error("OAuth callback does not contain code.")
    }

    suspend fun logout() {
        sessionStore.clear()
    }
}

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
