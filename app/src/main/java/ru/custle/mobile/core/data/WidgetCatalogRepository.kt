package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.WidgetCatalogDto
import ru.custle.mobile.core.network.CustleApi

class WidgetCatalogRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<WidgetCatalogDto> = api.widgetCatalog().data

    suspend fun install(id: String) {
        api.installWidgetCatalog(id)
    }

    suspend fun uninstall(id: String) {
        api.uninstallWidgetCatalog(id)
    }
}
