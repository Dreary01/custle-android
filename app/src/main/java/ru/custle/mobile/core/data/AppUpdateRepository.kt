package ru.custle.mobile.core.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.custle.mobile.BuildConfig
import ru.custle.mobile.core.model.AppUpdateMetadata
import java.io.File

class AppUpdateRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()

    fun installedVersionCode(): Int =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).longVersionCode.toInt()

    fun installedVersionName(): String =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName.orEmpty()

    fun canRequestPackageInstalls(): Boolean =
        appContext.packageManager.canRequestPackageInstalls()

    suspend fun check(): AppUpdateCheckResult {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(BuildConfig.UPDATE_METADATA_URL)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Не удалось проверить обновление: HTTP ${response.code}")
                }
                val body = response.body?.string().orEmpty()
                val metadata = json.decodeFromString<AppUpdateMetadata>(body)
                AppUpdateCheckResult(
                    currentVersionCode = installedVersionCode(),
                    currentVersionName = installedVersionName(),
                    latest = metadata,
                    updateAvailable = metadata.versionCode > installedVersionCode(),
                )
            }
        }
    }

    suspend fun downloadLatestApk(apkUrl: String): File {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(apkUrl)
                .get()
                .build()

            val targetDir = File(appContext.cacheDir, "updates").apply { mkdirs() }
            val targetFile = File(targetDir, "custle-update.apk")
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Не удалось скачать APK: HTTP ${response.code}")
                }
                val body = response.body ?: error("Пустой ответ при скачивании APK")
                targetFile.outputStream().use { output ->
                    body.byteStream().use { input -> input.copyTo(output) }
                }
            }
            targetFile
        }
    }

    fun buildInstallIntent(apkFile: File): Intent {
        val apkUri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            apkFile,
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

data class AppUpdateCheckResult(
    val currentVersionCode: Int,
    val currentVersionName: String,
    val latest: AppUpdateMetadata,
    val updateAvailable: Boolean,
)
