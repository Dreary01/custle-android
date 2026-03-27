package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.custle.mobile.core.model.ObjectTypeDto
import ru.custle.mobile.core.model.RequisiteDto
import ru.custle.mobile.core.model.RequisiteGroupDto
import ru.custle.mobile.core.network.CustleApi

class SchemaRepository(
    private val api: CustleApi,
) {
    suspend fun load(): SchemaBundle = coroutineScope {
        val objectTypes = async { api.objectTypes().data }
        val requisites = async { api.requisites().data }
        val groups = async { api.requisiteGroups().data }
        SchemaBundle(
            objectTypes = objectTypes.await(),
            requisites = requisites.await(),
            groups = groups.await(),
        )
    }

    suspend fun objectType(id: String): ObjectTypeDto = api.objectType(id).data
}

data class SchemaBundle(
    val objectTypes: List<ObjectTypeDto> = emptyList(),
    val requisites: List<RequisiteDto> = emptyList(),
    val groups: List<RequisiteGroupDto> = emptyList(),
)
