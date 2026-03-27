package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.SearchRequest
import ru.custle.mobile.core.model.SearchResultDto
import ru.custle.mobile.core.network.CustleApi

class SearchRepository(
    private val api: CustleApi,
) {
    suspend fun search(query: String, topK: Int = 20): List<SearchResultDto> =
        api.search(SearchRequest(query = query, topK = topK)).data
}
