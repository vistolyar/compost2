package com.example.compost2.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CompostApi {

    @Multipart
    @POST("api/process-audio") // Это будет адрес нашей функции на Vercel
    suspend fun uploadAudio(
        @Part audio: MultipartBody.Part,      // Сам файл
        @Part("prompt") prompt: RequestBody,  // Текст промпта (персона)
        @Part("openai_key") apiKey: RequestBody // API ключ (передаем с клиента для MVP)
    ): ArticleResponse

}