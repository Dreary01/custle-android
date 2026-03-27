package ru.custle.mobile.core.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val session = runBlocking { sessionStore.sessionFlow.first() }
        val request = chain.request().newBuilder().apply {
            session?.token?.let { header("Authorization", "Bearer $it") }
        }.build()
        return chain.proceed(request)
    }
}
