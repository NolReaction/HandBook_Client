package com.example.kursach_handbook.data.token

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken(context)
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Если токен есть, добавляем его в заголовок
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
