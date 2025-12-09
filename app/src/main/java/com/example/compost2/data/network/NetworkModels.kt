package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// НОВЫЙ КЛАСС ЗАПРОСА
data class ArticleRequest(
    @SerializedName("audio_base64") val audioBase64: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("openai_key") val openaiKey: String
)

// Ответ от сервера
data class ArticleResponse(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("status") val status: String? = "success"
)

data class ErrorResponse(
    @SerializedName("error") val error: String
)