package ru.custle.mobile.core.data

import ru.custle.mobile.core.model.ArticleDto
import ru.custle.mobile.core.model.NoteDto
import ru.custle.mobile.core.model.UpsertArticleRequest
import ru.custle.mobile.core.model.UpsertNoteRequest
import ru.custle.mobile.core.network.CustleApi

class KnowledgeRepository(
    private val api: CustleApi,
) {
    suspend fun notes(): List<NoteDto> = api.notes().data

    suspend fun note(id: String): NoteDto = api.note(id).data

    suspend fun createNote(
        title: String,
        content: String,
        tags: String,
        isPrivate: Boolean,
    ): NoteDto = api.createNote(
        UpsertNoteRequest(
            title = title,
            content = content,
            tags = tags,
            isPrivate = isPrivate,
        ),
    ).data

    suspend fun updateNote(
        id: String,
        title: String,
        content: String,
        tags: String,
        isPrivate: Boolean,
    ): NoteDto = api.updateNote(
        id = id,
        body = UpsertNoteRequest(
            title = title,
            content = content,
            tags = tags,
            isPrivate = isPrivate,
        ),
    ).data

    suspend fun deleteNote(id: String) {
        api.deleteNote(id)
    }

    suspend fun articles(): List<ArticleDto> = api.articles().data

    suspend fun article(id: String): ArticleDto = api.article(id).data

    suspend fun createArticle(
        title: String,
        content: String,
        category: String,
        tags: String,
        isPublished: Boolean,
    ): ArticleDto = api.createArticle(
        UpsertArticleRequest(
            title = title,
            content = content,
            category = category,
            tags = tags,
            isPublished = isPublished,
        ),
    ).data

    suspend fun updateArticle(
        id: String,
        title: String,
        content: String,
        category: String,
        tags: String,
        isPublished: Boolean,
    ): ArticleDto = api.updateArticle(
        id = id,
        body = UpsertArticleRequest(
            title = title,
            content = content,
            category = category,
            tags = tags,
            isPublished = isPublished,
        ),
    ).data

    suspend fun deleteArticle(id: String) {
        api.deleteArticle(id)
    }
}
