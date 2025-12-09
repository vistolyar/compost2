package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class PublicationViewModel(private val context: Context) : ViewModel() {

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    // Список категорий (пока фиктивный, позже будем тянуть из WP)
    val categories = listOf(
        "Uncategorized",
        "Personal Blog",
        "Technology & AI",
        "Business Ideas",
        "Travel Diaries",
        "Crypto News",
        "Marketing Tips"
    )

    // Выбранные категории
    var selectedCategories by mutableStateOf(setOf<String>())
        private set

    // Состояния загрузки
    var isPublishing by mutableStateOf(false)
    var isSavingDraft by mutableStateOf(false)

    // Сигналы завершения
    var isPublishedSuccess by mutableStateOf(false)
    var isDeletedSuccess by mutableStateOf(false)

    fun loadRecording(fileName: String) {
        val file = File(context.cacheDir, fileName)
        if (file.exists()) {
            recordingItem = RecordingItem(
                id = file.name,
                name = parseFileNameToDisplay(file.name),
                status = RecordingStatus.READY, // Мы знаем, что сюда приходят только Ready
                filePath = file.absolutePath,
                articleTitle = "Generated Article from ${parseFileNameToDisplay(file.name)}" // Заглушка, если нет заголовка
            )
        }
    }

    fun toggleCategory(category: String) {
        selectedCategories = if (selectedCategories.contains(category)) {
            selectedCategories - category
        } else {
            selectedCategories + category
        }
    }

    //  Publish - отправляет на WP и меняет статус
    fun publishPost() {
        if (isPublishing) return
        isPublishing = true

        viewModelScope.launch {
            delay(1500) // Симуляция сети
            isPublishing = false
            isPublishedSuccess = true
        }
    }

    //  Save Draft - просто сохраняет на WP, статус локальный не меняет
    fun saveDraft() {
        if (isSavingDraft) return
        isSavingDraft = true

        viewModelScope.launch {
            delay(1000) // Симуляция
            isSavingDraft = false
            // Тут мы бы показали Toast "Draft Saved", но статус карточки не меняем
        }
    }

    //  Delete
    fun deletePost() {
        recordingItem?.let { item ->
            val file = File(item.filePath)
            if (file.exists()) file.delete()
            isDeletedSuccess = true
        }
    }

    private fun parseFileNameToDisplay(fileName: String): String {
        return fileName.substringBeforeLast(".") // Упрощенный парсер
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PublicationViewModel(context)
            }
        }
    }
}