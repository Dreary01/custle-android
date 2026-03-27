package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.SuperadminStatsDto
import ru.custle.mobile.core.model.SuperadminUserDto
import ru.custle.mobile.core.model.SuperadminWorkspaceDto
import ru.custle.mobile.core.network.CustleApi

class SuperadminRepository(
    private val api: CustleApi,
) {
    suspend fun bootstrap(): SuperadminBundle = coroutineScope {
        val stats = async { api.superadminStats().data }
        val workspaces = async { api.superadminWorkspaces().data }
        val users = async { api.superadminUsers().data }
        val settings = async { api.superadminSettings().data }
        SuperadminBundle(
            stats = stats.await(),
            workspaces = workspaces.await(),
            users = users.await(),
            settings = settings.await(),
        )
    }
}

data class SuperadminBundle(
    val stats: SuperadminStatsDto = SuperadminStatsDto(),
    val workspaces: List<SuperadminWorkspaceDto> = emptyList(),
    val users: List<SuperadminUserDto> = emptyList(),
    val settings: Map<String, JsonElement> = emptyMap(),
)
