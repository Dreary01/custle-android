package ru.custle.mobile.core.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.custle.mobile.core.model.DashboardSnapshot
import ru.custle.mobile.core.network.CustleApi

class DashboardRepository(
    private val api: CustleApi,
) {
    suspend fun snapshot(): DashboardSnapshot = coroutineScope {
        val requests = async { api.dashboardRequests().data }
        val directions = async { api.dashboardDirections().data }
        val events = async { api.dashboardEvents().data }

        DashboardSnapshot(
            requests = requests.await(),
            directions = directions.await(),
            events = events.await(),
        )
    }
}
