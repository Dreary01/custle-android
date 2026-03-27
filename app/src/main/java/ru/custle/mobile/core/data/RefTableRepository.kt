package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.custle.mobile.core.model.RefRecordDto
import ru.custle.mobile.core.model.RefTableDto
import ru.custle.mobile.core.network.CustleApi

class RefTableRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<RefTableDto> = api.refTables().data

    suspend fun detail(tableId: String): RefTableBundle = coroutineScope {
        val table = async { api.refTable(tableId).data }
        val records = async { runCatching { api.refTableRecords(tableId).data }.getOrDefault(emptyList()) }
        RefTableBundle(
            table = table.await(),
            records = records.await(),
        )
    }
}

data class RefTableBundle(
    val table: RefTableDto,
    val records: List<RefRecordDto> = emptyList(),
)
