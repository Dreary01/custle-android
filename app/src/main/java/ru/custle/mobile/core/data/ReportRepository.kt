package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.ReportDto
import ru.custle.mobile.core.network.CustleApi

class ReportRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<ReportDto> = api.reports().data
}
