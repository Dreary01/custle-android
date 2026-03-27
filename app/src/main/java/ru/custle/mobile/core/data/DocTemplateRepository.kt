package ru.custle.mobile.core.data

import android.content.Context
import okhttp3.ResponseBody
import ru.custle.mobile.core.model.DocTemplateDto
import ru.custle.mobile.core.network.CustleApi
import java.io.File

class DocTemplateRepository(
    private val context: Context,
    private val api: CustleApi,
) {
    suspend fun list(
        objectTypeId: String? = null,
        requisiteId: String? = null,
    ): List<DocTemplateDto> = api.documentTemplates(typeId = objectTypeId, requisiteId = requisiteId).data

    suspend fun detail(id: String): DocTemplateDto = api.documentTemplate(id).data

    suspend fun download(id: String, fileName: String): File {
        val body = api.downloadDocumentTemplate(id)
        return body.use { responseBody -> writeToCache(fileName, responseBody) }
    }

    suspend fun generate(
        objectId: String,
        templateId: String,
        fileName: String,
        format: String? = null,
    ): File {
        val body = api.generateDocument(objectId = objectId, templateId = templateId, format = format)
        return body.use { responseBody -> writeToCache(fileName, responseBody) }
    }

    private fun writeToCache(fileName: String, body: ResponseBody): File {
        val dir = File(context.cacheDir, "custle-template-documents")
        if (!dir.exists()) dir.mkdirs()

        val safeName = fileName.ifBlank { "template.docx" }.replace(Regex("""[\\/:*?"<>|]"""), "_")
        val target = File(dir, safeName)

        body.byteStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }
}
