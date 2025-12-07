package com.example.compost2.domain

enum class RecordingStatus {
    SAVED,      // 1. Просто запись (Синяя)
    PROCESSING, // 2. В обработке (Синяя, как ты просил)
    READY,      // 3. Готово к публикации (Желтая)
    PUBLISHED   // 4. Опубликовано (Зеленая)
}

data class RecordingItem(
    val id: String,
    val name: String,
    val status: RecordingStatus,
    val filePath: String,
    val articleTitle: String? = null,
    val promptName: String? = null,
    val publicUrl: String? = null // Ссылка на статью в WP
)