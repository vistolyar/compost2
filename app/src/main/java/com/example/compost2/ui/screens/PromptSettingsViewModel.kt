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
import com.example.compost2.domain.PromptItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PromptSettingsViewModel(context: Context) : ViewModel() {

    private val repository = PromptsRepository(context)

    // Текущий список
    var prompts by mutableStateOf(emptyList<PromptItem>())
        private set

    // Состояние для диалога редактирования/создания
    var showDialog by mutableStateOf(false)
    var currentEditingId: String? = null // Если null - значит создаем новый
    var editTitle by mutableStateOf("")
    var editContent by mutableStateOf("")

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        prompts = repository.getPrompts()
    }

    fun deletePrompt(id: String) {
        repository.deletePrompt(id)
        loadPrompts()
    }

    // Открыть диалог создания
    fun openCreateDialog() {
        currentEditingId = null
        editTitle = ""
        editContent = ""
        showDialog = true
    }

    // Открыть диалог редактирования
    fun openEditDialog(item: PromptItem) {
        currentEditingId = item.id
        editTitle = item.title
        editContent = item.content
        showDialog = true
    }

    // Сохранить (из диалога)
    fun saveFromDialog() {
        val timestamp = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

        if (currentEditingId == null) {
            // Создание нового
            val newItem = PromptItem(
                id = UUID.randomUUID().toString(),
                title = editTitle,
                content = editContent,
                isDraft = false,
                lastModified = timestamp
            )
            repository.addPrompt(newItem)
        } else {
            // Обновление старого
            val updatedItem = PromptItem(
                id = currentEditingId!!,
                title = editTitle,
                content = editContent,
                isDraft = false,
                lastModified = timestamp
            )
            repository.updatePrompt(updatedItem)
        }

        loadPrompts()
        showDialog = false
    }

    fun closeDialog() {
        showDialog = false
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PromptSettingsViewModel(context)
            }
        }
    }
}