package ru.custle.mobile.core.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import ru.custle.mobile.core.model.DocumentFileDto
import ru.custle.mobile.core.model.DocumentIndexStatusDto
import ru.custle.mobile.core.model.DocumentInfoDto
import ru.custle.mobile.core.model.DeleteDocumentsRequest
import ru.custle.mobile.core.model.UpdateDocumentRequest
import ru.custle.mobile.core.network.CustleApi
import java.io.File

class DocumentRepository(
    private val context: Context,
    private val api: CustleApi,
) {
    suspend fun load(objectId: String): DocumentBundle = coroutineScope {
        val files = async { api.documentFiles(objectId).data }
        val indexStatus = async { api.documentIndexStatus(objectId).data }
        val info = async { runCatching { api.documentInfo(objectId).data }.getOrDefault(DocumentInfoDto()) }

        DocumentBundle(
            files = files.await(),
            indexStatus = indexStatus.await(),
            info = info.await(),
        )
    }

    suspend fun download(docId: String, fileName: String): File {
        val body = api.downloadDocument(docId)
        return body.use { responseBody -> writeToCache(fileName, responseBody) }
    }

    suspend fun upload(objectId: String, uri: Uri): String {
        val fileName = resolveDisplayName(uri)
        val mimeType = context.contentResolver.getType(uri).orEmpty()
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Не удалось прочитать выбранный файл")
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("upload", fileName, requestBody)
        return api.uploadDocument(id = objectId, upload = part).data.docId
    }

    suspend fun rename(objectId: String, docId: String, name: String) {
        api.updateDocument(
            id = objectId,
            body = UpdateDocumentRequest(
                operation = "rename",
                name = name,
                docId = docId,
            ),
        )
    }

    suspend fun delete(objectId: String, docId: String) {
        api.deleteDocuments(
            id = objectId,
            body = DeleteDocumentsRequest(ids = listOf(docId)),
        )
    }

    suspend fun reindex(objectId: String): Int =
        api.reindexDocuments(objectId).data.queued

    private fun writeToCache(fileName: String, body: ResponseBody): File {
        val dir = File(context.cacheDir, "custle-documents")
        if (!dir.exists()) dir.mkdirs()

        val safeName = fileName.ifBlank { "document.bin" }.replace(Regex("""[\\/:*?"<>|]"""), "_")
        val target = File(dir, safeName)

        body.byteStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }

    private fun resolveDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index).orEmpty().ifBlank { "document.bin" }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "document.bin" } ?: "document.bin"
    }
}

data class DocumentBundle(
    val files: List<DocumentFileDto> = emptyList(),
    val indexStatus: List<DocumentIndexStatusDto> = emptyList(),
    val info: DocumentInfoDto = DocumentInfoDto(),
)
