package ru.custle.mobile.core.data

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toDebugMessage(context: String? = null): String {
    val prefix = context?.takeIf { it.isNotBlank() }?.let { "$it\n" }.orEmpty()
    val body = when (this) {
        is HttpException -> {
            val response = response()
            val url = response?.raw()?.request?.url?.toString().orEmpty()
            val errorBody = runCatching { response?.errorBody()?.string() }.getOrNull().orEmpty().trim()
            buildString {
                append("HTTP ${code()}")
                if (url.isNotBlank()) append(" • $url")
                if (message().isNotBlank()) append("\n${message()}")
                if (errorBody.isNotBlank()) append("\n$errorBody")
            }
        }

        is SerializationException -> {
            "Serialization error\n${message ?: javaClass.simpleName}"
        }

        is IOException -> {
            "Network/IO error\n${message ?: javaClass.simpleName}"
        }

        else -> {
            buildString {
                append(javaClass.simpleName)
                if (!message.isNullOrBlank()) append("\n$message")
                cause?.message?.takeIf { it.isNotBlank() }?.let { append("\nCause: $it") }
            }
        }
    }.ifBlank {
        buildString {
            append(this@toDebugMessage.javaClass.simpleName)
            this@toDebugMessage.message?.takeIf { it.isNotBlank() }?.let { append("\n$it") }
        }
    }
    return prefix + body
}
