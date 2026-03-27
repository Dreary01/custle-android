package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.NewsDto
import ru.custle.mobile.core.network.CustleApi

class NewsRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<NewsDto> = api.news().data
}
