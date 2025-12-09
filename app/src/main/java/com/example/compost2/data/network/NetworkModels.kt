package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// Ответ от сервера: Готовая статья
data class ArticleResponse(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    // Можно добавить статус, если нужно
    @SerializedName("status") val status: String? = "success"
)

// Ответ при ошибке (опционально)
data class ErrorResponse(
    @SerializedName("error") val error: String
)