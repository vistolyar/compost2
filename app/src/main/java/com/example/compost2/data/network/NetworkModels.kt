package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// --- НОВЫЕ МОДЕЛИ ДЛЯ S3 ---

// Ответ на запрос "Куда грузить?"
data class UploadUrlResponse(
    @SerializedName("upload_url") val uploadUrl: String, // Ссылка на S3 (PUT)
    @SerializedName("file_key") val fileKey: String,     // ID файла для бэкенда
    @SerializedName("expires_in") val expiresIn: Int? = 3600
)

// --- ОБНОВЛЕННЫЙ ЗАПРОС НА ПРОЦЕССИНГ ---
data class ArticleRequest(
    // Base64 делаем nullable (он теперь запасной вариант)
    @SerializedName("audio_base64") val audioBase64: String? = null,

    // НОВОЕ ПОЛЕ: Ключ файла в S3
    @SerializedName("file_key") val fileKey: String? = null,

    @SerializedName("prompt") val prompt: String,
    @SerializedName("openai_key") val openaiKey: String
)

// Ответ от сервера (остается прежним, но добавим поля для надежности)
data class ArticleResponse(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("status") val status: String? = "success"
)

data class ErrorResponse(
    @SerializedName("error") val error: String
)