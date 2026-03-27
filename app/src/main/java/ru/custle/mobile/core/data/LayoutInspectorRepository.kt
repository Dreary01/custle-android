package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.GridStateDto
import ru.custle.mobile.core.model.WidgetLayoutDto
import ru.custle.mobile.core.network.CustleApi

class LayoutInspectorRepository(
    private val api: CustleApi,
) {
    suspend fun widgetLayouts(
        pageType: String,
        objectId: String? = null,
        typeId: String? = null,
    ): List<WidgetLayoutDto> = api.widgetLayouts(pageType = pageType, objectId = objectId, typeId = typeId).data

    suspend fun gridState(
        gridId: String,
        objectId: String? = null,
        typeId: String? = null,
    ): GridStateDto = api.gridState(gridId = gridId, objectId = objectId, typeId = typeId).data
}
