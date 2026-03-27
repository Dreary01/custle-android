package ru.custle.mobile.core.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File

object DocumentIntentHelper {
    fun createOpenIntent(context: Context, sourceFile: File, displayName: String): Intent? {
        val uri = exportToPublicStorage(context, sourceFile, displayName) ?: return null
        val mimeType = mimeType(displayName)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createShareIntent(context: Context, sourceFile: File, displayName: String): Intent? {
        val uri = exportToPublicStorage(context, sourceFile, displayName) ?: return null
        val mimeType = mimeType(displayName)
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun exportToPublicStorage(context: Context, sourceFile: File, displayName: String): Uri? {
        if (!sourceFile.exists()) return null

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val safeName = sanitizeDisplayName(displayName)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, uniqueDisplayName(context, safeName))
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType(displayName))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Custle")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(collection, values) ?: return null
        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }.also { pendingValues ->
                    context.contentResolver.update(uri, pendingValues, null, null)
                }
            } else {
                MediaScannerConnection.scanFile(context, arrayOf(sourceFile.absolutePath), null, null)
            }
            return uri
        } catch (_: Throwable) {
            runCatching { context.contentResolver.delete(uri, null, null) }
            return null
        }
    }

    private fun uniqueDisplayName(context: Context, baseName: String): String {
        val dir = File(context.cacheDir, "custle-documents")
        val suffix = baseName.substringAfterLast('.', "")
        val stem = baseName.removeSuffix(if (suffix.isNotBlank()) ".$suffix" else "")
        var candidate = baseName
        var index = 1
        while (File(dir, candidate).exists()) {
            candidate = if (suffix.isNotBlank()) "$stem-$index.$suffix" else "$stem-$index"
            index++
        }
        return candidate
    }

    private fun sanitizeDisplayName(value: String): String =
        value.ifBlank { "document.bin" }.replace(Regex("""[\\/:*?"<>|]"""), "_")

    private fun mimeType(value: String): String {
        val extension = value.substringAfterLast('.', "").lowercase().takeIf { it.isNotBlank() }
        return extension?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
            ?: "application/octet-stream"
    }
}
