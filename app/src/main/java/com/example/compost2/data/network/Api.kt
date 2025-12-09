package com.example.compost2.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CompostApi {

    @GET("api/ping")
    suspend fun ping(): Any

    // ИЗМЕНЕНО: Теперь это обычный POST с JSON телом
    @POST("api/process-audio")
    suspend fun uploadAudio(
        @Body request: ArticleRequest
    ): ArticleResponse
}