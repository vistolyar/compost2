package com.example.compost2.domain

enum class RecordingStatus {
    SAVED, PROCESSING, READY, PUBLISHED
}

data class RecordingItem(
    val id: String,
    val name: String,
    val status: RecordingStatus,
    val filePath: String,
    val articleTitle: String? = null,
    val articleContent: String? = null,
    val promptName: String? = null,
    val publicUrl: String? = null,
    val wordpressId: Int? = null // НОВОЕ ПОЛЕ: ID поста в WordPress
)

data class PromptItem(
    val id: String,
    val title: String,
    val content: String,
    val isDraft: Boolean = false,
    val lastModified: String
)