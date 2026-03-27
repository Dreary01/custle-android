package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import ru.custle.mobile.core.model.MarketplaceConfigDto
import ru.custle.mobile.core.model.MarketplaceInstallationDto
import ru.custle.mobile.core.network.CustleApi

class MarketplaceRepository(
    private val api: CustleApi,
) {
    suspend fun bootstrap(): MarketplaceBundle = coroutineScope {
        val configs = async { api.marketplaceConfigs().data }
        val installations = async { api.marketplaceInstallations().data }
        MarketplaceBundle(
            configs = configs.await(),
            installations = installations.await(),
        )
    }

    suspend fun syncStatus(id: String): JsonElement = api.marketplaceSyncStatus(id).data
}

data class MarketplaceBundle(
    val configs: List<MarketplaceConfigDto> = emptyList(),
    val installations: List<MarketplaceInstallationDto> = emptyList(),
)
