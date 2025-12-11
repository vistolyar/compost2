package com.example.compost2.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path // Импорт для подстановки ID

interface WpApi {

    @GET("wp-json/wp/v2/categories?per_page=100")
    suspend fun getCategories(): List<WpCategory>

    // Создание (как раньше)
    @POST("wp-json/wp/v2/posts")
    suspend fun createPost(
        @Body request: WpPostRequest
    ): WpPostResponse

    // НОВЫЙ МЕТОД: Обновление по ID
    @POST("wp-json/wp/v2/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body request: WpPostRequest
    ): WpPostResponse
}