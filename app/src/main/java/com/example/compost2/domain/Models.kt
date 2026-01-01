package com.example.compost2.domain

enum class RecordingStatus {
    SAVED, PROCESSING, READY, PUBLISHED, TRANSCRIBED // Добавлен новый статус
}

// Новые типы интеграций для иконок
enum class IntegrationType {
    CALENDAR, GMAIL, WORDPRESS, TASKS
}

data class RecordingItem(
    val id: String,
    val name: String, // Это имя файла или черновой заголовок
    val status: RecordingStatus,
    val filePath: String,

    // Результаты работы AI
    val articleTitle: String? = null,
    val articleContent: String? = null,

    // Метаданные
    val promptName: String? = null,
    val publicUrl: String? = null,
    val wordpressId: Int? = null,

    // НОВОЕ: Список выполненных интеграций (для иконок на карточке)
    val completedIntegrations: List<IntegrationType> = emptyList()
)

data class PromptItem(
    val id: String,
    val title: String,
    val content: String,
    val isDraft: Boolean = false,
    val lastModified: String
)