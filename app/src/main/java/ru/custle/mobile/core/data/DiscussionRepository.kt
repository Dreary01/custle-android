package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.DiscussionDto
import ru.custle.mobile.core.model.DiscussionMessageDto
import ru.custle.mobile.core.network.CustleApi

class DiscussionRepository(
    private val api: CustleApi,
) {
    suspend fun list(objectId: String): List<DiscussionDto> = api.objectDiscussions(objectId).data

    suspend fun nested(discussionId: String): List<DiscussionDto> = api.nestedDiscussions(discussionId).data

    suspend fun messages(discussionId: String): List<DiscussionMessageDto> = api.discussionMessages(discussionId).data

    suspend fun createDiscussion(objectId: String, title: String): String =
        api.createDiscussion(objectId, ru.custle.mobile.core.model.CreateDiscussionRequest(title)).data["id"].orEmpty()

    suspend fun createNestedDiscussion(
        objectId: String,
        title: String,
        parentDiscussionId: String,
        parentMessageId: String,
    ): String =
        api.createDiscussion(
            objectId,
            ru.custle.mobile.core.model.CreateDiscussionRequest(
                title = title,
                parentDiscussionId = parentDiscussionId,
                parentMessageId = parentMessageId,
            ),
        ).data["id"].orEmpty()

    suspend fun updateDiscussion(
        discussionId: String,
        title: String? = null,
        isClosed: Boolean? = null,
    ): String =
        api.updateDiscussion(
            discussionId,
            ru.custle.mobile.core.model.UpdateDiscussionRequest(
                title = title,
                isClosed = isClosed,
            ),
        ).data["id"].orEmpty()

    suspend fun deleteDiscussion(discussionId: String) {
        api.deleteDiscussion(discussionId)
    }

    suspend fun createMessage(
        discussionId: String,
        content: String,
        mentionedUserIds: List<String> = emptyList(),
        isRequest: Boolean = false,
    ): String =
        api.createDiscussionMessage(
            discussionId,
            ru.custle.mobile.core.model.CreateDiscussionMessageRequest(
                content = content,
                mentionedUserIds = mentionedUserIds,
                isRequest = isRequest,
            ),
        ).data["id"].orEmpty()

    suspend fun updateMessage(
        messageId: String,
        content: String,
    ): String =
        api.updateDiscussionMessage(
            messageId,
            ru.custle.mobile.core.model.UpdateDiscussionMessageRequest(content = content),
        ).data["id"].orEmpty()

    suspend fun deleteMessage(messageId: String) {
        api.deleteDiscussionMessage(messageId)
    }
}
