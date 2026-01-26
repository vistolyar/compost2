package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// --- SHARED / UTILS ---

// Ответ на получение ссылки загрузки (GET /api/get_upload_url)
data class UploadUrlResponse(
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("file_key") val fileKey: String
)

// --- STAGE 1: TRANSCRIPTION ---

// Запрос на транскрибацию (POST /api/transcribe)
data class TranscribeRequest(
    @SerializedName("file_key") val fileKey: String,
    @SerializedName("openai_key") val openAiKey: String
)

// Ответ транскрибации
data class TranscribeResponse(
    @SerializedName("raw_text") val rawText: String
)

// --- STAGE 2: PROCESSING ---

// Запрос на обработку текста (POST /api/process-text)
data class ProcessTextRequest(
    @SerializedName("raw_text") val rawText: String,
    @SerializedName("prompt") val prompt: String,
    @SerializedName("openai_key") val openAiKey: String
)

// Ответ обработки (GPT результат)
// Используется также для старого формата, если нужно, но по сути это результат Stage 2
data class ArticleResponse(
    val title: String,
    val content: String
)