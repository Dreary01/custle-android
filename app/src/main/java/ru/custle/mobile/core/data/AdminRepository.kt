package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.AdminUserDto
import ru.custle.mobile.core.model.PermissionDto
import ru.custle.mobile.core.network.CustleApi

class AdminRepository(
    private val api: CustleApi,
) {
    suspend fun bootstrap(): AdminBundle = coroutineScope {
        val users = async { api.adminUsers().data }
        val settings = async { api.adminSettings().data }
        val modules = async { runCatching { api.modules().data }.getOrDefault(emptyMap<String, JsonElement>()) }
        AdminBundle(
            users = users.await(),
            settings = settings.await(),
            modules = modules.await(),
        )
    }

    suspend fun permissionsByUser(userId: String): List<PermissionDto> = api.permissions(userId = userId).data
}

data class AdminBundle(
    val users: List<AdminUserDto> = emptyList(),
    val settings: Map<String, JsonElement> = emptyMap(),
    val modules: Map<String, JsonElement> = emptyMap(),
)
