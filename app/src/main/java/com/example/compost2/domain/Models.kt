package com.example.compost2.domain

enum class RecordingStatus {
    PENDING,    // В обработке (сигнализирует о процессе)
    DRAFT,      // Черновик (желтый)
    PUBLISHED,  // Опубликовано (зеленый)
    SAVED       // Сохранено локально, готово к отправке
}

data class RecordingItem(
    val id: String,
    val name: String,              // Название (например, дата 172422112025)
    val status: RecordingStatus,
    val date: String,
    val filePath: String? = null,  // Путь к файлу на устройстве (нужен для плеера)
    val progress: Float? = null    // Для статуса Pending (0.0 - 1.0)
)