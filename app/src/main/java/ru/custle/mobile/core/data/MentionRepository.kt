package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.MentionDto
import ru.custle.mobile.core.network.CustleApi

class MentionRepository(
    private val api: CustleApi,
) {
    suspend fun list(): List<MentionDto> = api.myMentions().data

    suspend fun resolve(id: String) {
        api.resolveMention(id)
    }
}
