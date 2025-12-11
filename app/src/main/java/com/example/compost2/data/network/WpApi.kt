package com.example.compost2.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WpApi {

    // Скачать список категорий (берем 100 штук, чтобы наверняка хватило)
    @GET("wp-json/wp/v2/categories?per_page=100")
    suspend fun getCategories(): List<WpCategory>

    // Создать пост
    @POST("wp-json/wp/v2/posts")
    suspend fun createPost(
        @Body request: WpPostRequest
    ): WpPostResponse
}