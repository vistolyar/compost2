package com.example.compost2.domain

enum class RecordingStatus {
    SAVED, PROCESSING, READY, PUBLISHED, TRANSCRIBED
}

enum class IntegrationType {
    NONE, CALENDAR, GMAIL, TASKS, WORDPRESS
}

data class RecordingItem(
    val id: String,
    val name: String,
    val status: RecordingStatus,
    val filePath: String,

    // Рабочий текст (Current Output)
    val articleTitle: String? = null,
    val articleContent: String? = null,

    // Сырой исходник (Source of Truth)
    val rawTranscription: String? = null,

    val promptName: String? = null,
    val publicUrl: String? = null,
    val wordpressId: Int? = null,
    val completedIntegrations: List<IntegrationType> = emptyList()
)

data class PromptItem(
    val id: String,
    val title: String,
    val content: String,
    val integrationType: IntegrationType = IntegrationType.NONE,
    val isDraft: Boolean = false, // Оставляем поле для совместимости, но в UI игнорируем
    val lastModified: String,

    // НОВЫЕ ПОЛЯ
    val usageCount: Int = 0,
    val lastUsed: String? = null // Храним дату последнего использования
)