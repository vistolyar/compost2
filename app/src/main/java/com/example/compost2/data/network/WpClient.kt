package com.example.compost2.data.network

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WpClient {

    // Функция создает API "на лету" под конкретный сайт
    fun create(siteUrl: String, username: String, appPassword: String): WpApi {

        // 1. Формируем "Паспорт" (Basic Auth Header)
        // Формат: "username:password" -> Base64
        val credentials = "$username:$appPassword"
        val authHeader = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", authHeader) // Показываем паспорт
                    .header("Accept", "application/json")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        // Убеждаемся, что URL заканчивается на слэш (Retrofit это требует)
        val finalUrl = if (siteUrl.endsWith("/")) siteUrl else "$siteUrl/"

        return Retrofit.Builder()
            .baseUrl(finalUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WpApi::class.java)
    }
}