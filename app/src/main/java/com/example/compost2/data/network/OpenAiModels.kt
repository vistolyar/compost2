package com.example.compost2.data.network

import com.google.gson.annotations.SerializedName

// --- ЗАПРОС ---
data class ChatRequest(
    val model: String = "gpt-4o", // Или "gpt-3.5-turbo", если экономим
    val messages: List<Message>,
    @SerializedName("response_format") val responseFormat: ResponseFormat = ResponseFormat(type = "json_object")
)

data class Message(
    val role: String, // "system" или "user"
    val content: String
)

data class ResponseFormat(
    val type: String
)

// --- ОТВЕТ ---
data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// --- НАШ ФОРМАТ ДАННЫХ (ТО, ЧТО МЫ ЖДЕМ ОТ AI) ---
// AI вернет JSON, который мы распарсим в этот класс
data class AiActionResponse(
    @SerializedName("action_type") val actionType: String, // "CALENDAR", "GMAIL", "NONE"
    @SerializedName("title") val title: String?,
    @SerializedName("body") val body: String?, // Описание события или текст письма
    @SerializedName("date") val date: String? // "YYYY-MM-DD HH:MM" (для календаря)
)