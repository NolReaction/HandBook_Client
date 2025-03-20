package com.example.kursach_handbook.data.remote

import android.content.Context
import com.example.kursach_handbook.data.token.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val HTTPS_URL = "https://176.114.71.165:8443/"
    private const val HTTP_URL = "http://176.114.71.165:8080/"
    private const val TEST_URL = "http://10.0.2.2:8080/"


    fun createRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(HTTP_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}
