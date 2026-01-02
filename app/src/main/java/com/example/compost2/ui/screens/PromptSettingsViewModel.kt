package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.PromptsRepository
import com.example.compost2.domain.IntegrationType
import com.example.compost2.domain.PromptItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PromptSettingsViewModel(private val repository: PromptsRepository) : ViewModel() {

    var prompts by mutableStateOf<List<PromptItem>>(emptyList())
        private set

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        prompts = repository.getPrompts()
    }

    // Метод для получения промпта при редактировании
    fun getPromptById(id: String): PromptItem? {
        return prompts.find { it.id == id }
    }

    fun savePrompt(id: String?, title: String, content: String, type: IntegrationType) {
        if (title.isBlank()) return

        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

        if (id == null) {
            // Создание нового
            val newItem = PromptItem(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                integrationType = type,
                lastModified = date
            )
            repository.addPrompt(newItem)
        } else {
            // Обновление существующего
            val existing = getPromptById(id) ?: return
            val updated = existing.copy(
                title = title,
                content = content,
                integrationType = type,
                lastModified = date
            )
            repository.updatePrompt(updated)
        }
        loadPrompts()
    }

    fun deletePrompt(id: String) {
        repository.deletePrompt(id)
        loadPrompts()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PromptSettingsViewModel(com.example.compost2.data.PromptsRepository(context))
            }
        }
    }
}