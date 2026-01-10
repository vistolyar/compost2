package com.example.compost2.data.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface CompostApi {

    @GET("api/ping")
    suspend fun ping(): Any

    // --- ШАГ 1: Получить ссылку для загрузки ---
    @GET("api/get_upload_url")
    suspend fun getUploadUrl(): UploadUrlResponse

    // --- ШАГ 2: Загрузить файл в S3 ---
    // @Url позволяет использовать динамическую ссылку, полученную на шаге 1
    // Мы не используем @Body ArticleRequest, мы шлем сырые байты (RequestBody)
    @PUT
    suspend fun uploadFileToS3(
        @Url url: String,
        @Body file: RequestBody,
        @Header("Content-Type") contentType: String = "audio/mp4"
    ): Response<Unit>

    // --- ШАГ 3: Сказать бэкенду "Готово, обрабатывай" ---
    @POST("api/process-audio")
    suspend fun processAudio(
        @Body request: ArticleRequest
    ): ArticleResponse
}