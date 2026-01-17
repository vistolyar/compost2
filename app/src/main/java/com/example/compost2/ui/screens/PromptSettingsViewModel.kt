package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

    var isSelectionMode by mutableStateOf(false)
        private set

    val selectedIds = mutableStateListOf<String>()

    init {
        loadPrompts()
    }

    fun loadPrompts() {
        prompts = repository.getPrompts()
    }

    fun getPromptById(id: String): PromptItem? {
        return prompts.find { it.id == id }
    }

    fun savePrompt(
        id: String?,
        title: String,
        content: String,
        type: IntegrationType,
        isActive: Boolean
    ) {
        if (title.isBlank()) return

        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())

        if (id == null) {
            val newItem = PromptItem(
                id = UUID.randomUUID().toString(),
                title = title,
                content = content,
                integrationType = type,
                lastModified = date,
                isActive = isActive,
                usageCount = 0,
                lastUsed = "Never"
            )
            repository.addPrompt(newItem)
        } else {
            val existing = getPromptById(id) ?: return
            val updated = existing.copy(
                title = title,
                content = content,
                integrationType = type,
                lastModified = date,
                isActive = isActive
            )
            repository.updatePrompt(updated)
        }
        loadPrompts()
    }

    fun deletePrompt(id: String) {
        repository.deletePrompt(id)
        loadPrompts()
    }

    fun toggleSelectionMode() {
        isSelectionMode = !isSelectionMode
        if (!isSelectionMode) selectedIds.clear()
    }

    fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
            if (selectedIds.isEmpty()) isSelectionMode = false
        } else {
            selectedIds.add(id)
        }
    }

    fun deleteSelected() {
        selectedIds.forEach { id -> repository.deletePrompt(id) }
        selectedIds.clear()
        isSelectionMode = false
        loadPrompts()
    }

    // НОВЫЙ МЕТОД: Массовое изменение статуса
    fun updateSelectedStatus(isActive: Boolean) {
        selectedIds.forEach { id ->
            val existing = getPromptById(id)
            if (existing != null) {
                repository.updatePrompt(existing.copy(isActive = isActive))
            }
        }
        selectedIds.clear()
        isSelectionMode = false
        loadPrompts()
    }

    fun sortPrompts(criteria: String) {
        val sorted = when (criteria) {
            "alpha" -> prompts.sortedBy { it.title }
            "usage" -> prompts.sortedByDescending { it.usageCount }
            else -> prompts
        }
        prompts = sorted
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PromptSettingsViewModel(PromptsRepository(context))
            }
        }
    }
}