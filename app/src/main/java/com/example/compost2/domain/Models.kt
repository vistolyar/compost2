package com.example.compost2.domain

enum class RecordingStatus {
    SAVED,      // 1. Просто запись
    PROCESSING, // 2. В обработке
    READY,      // 3. Готово (текст получен, можно править)
    PUBLISHED   // 4. Опубликовано
}

data class RecordingItem(
    val id: String,
    val name: String,
    val status: RecordingStatus,
    val filePath: String,
    val articleTitle: String? = null,
    val articleContent: String? = null, // НОВОЕ ПОЛЕ: Текст статьи
    val promptName: String? = null,
    val publicUrl: String? = null
)

data class PromptItem(
    val id: String,
    val title: String,
    val content: String,
    val isDraft: Boolean = false,
    val lastModified: String
)