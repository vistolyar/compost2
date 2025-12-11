package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// Категория (Рубрика)
data class WpCategory(
    val id: Int,
    val name: String,
    val count: Int // Количество постов в рубрике
)

// Запрос на создание поста
data class WpPostRequest(
    val title: String,
    val content: String,
    val status: String, // 'publish' или 'draft'
    val categories: List<Int> // ID выбранных рубрик
)

// Ответ сервера после создания
data class WpPostResponse(
    val id: Int,
    val link: String, // Ссылка на готовый пост
    @SerializedName("status") val status: String
)