package com.example.compost2.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Url

interface CompostApi {

    @GET("api/ping")
    suspend fun ping(): Map<String, String>

    // 1. Получить ссылку на S3
    @GET("api/get_upload_url")
    suspend fun getUploadUrl(): UploadUrlResponse

    // 2. Загрузить файл в S3 (прямой URL)
    @PUT
    suspend fun uploadFileToS3(
        @Url url: String,
        @Body file: RequestBody
    ): Response<ResponseBody>

    // --- NEW V2.0 API ---

    // 3. Шаг 1: Транскрибация (File Key -> Raw Text)
    @POST("api/transcribe")
    suspend fun transcribe(
        @Body request: TranscribeRequest
    ): TranscribeResponse

    // 4. Шаг 2: Процессинг (Raw Text + Prompt -> Title/Content)
    @POST("api/process-text")
    suspend fun processText(
        @Body request: ProcessTextRequest
    ): ArticleResponse
}