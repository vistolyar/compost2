package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.domain.PromptItem
import java.util.UUID

class PromptSettingsViewModel(private val context: Context) : ViewModel() {

    // Исходный список (имитация базы данных)
    private var allPrompts = mutableListOf(
        PromptItem("1", "Default Transcriber", "Just transcribe the audio exactly as is.", false, "Today"),
        PromptItem("2", "SEO Blog Post", "You are an SEO expert. Write a structured blog post...", false, "Yesterday"),
        PromptItem("3", "LinkedIn Virality", "Make it punchy, use emojis, focus on business...", true, "2 days ago"),
        PromptItem("4", "Summarizer", "Create a bullet-point summary of the recording.", false, "Last week")
    )

    // Список для отображения (фильтрованный)
    var visiblePrompts by mutableStateOf<List<PromptItem>>(emptyList())
        private set

    // Текущая вкладка: 0 = Published (All), 1 = Drafts
    var selectedTab by mutableStateOf(0)
        private set

    init {
        updateFilter()
    }

    fun selectTab(index: Int) {
        selectedTab = index
        updateFilter()
    }

    private fun updateFilter() {
        visiblePrompts = if (selectedTab == 0) {
            allPrompts.filter { !it.isDraft } // Показываем опубликованные
        } else {
            allPrompts.filter { it.isDraft }  // Показываем черновики
        }
    }

    fun deletePrompt(id: String) {
        allPrompts.removeIf { it.id == id }
        updateFilter()
    }

    // Заглушка для создания
    fun createNewPrompt() {
        val newPrompt = PromptItem(
            id = UUID.randomUUID().toString(),
            title = "New Prompt Draft",
            content = "",
            isDraft = true,
            lastModified = "Just now"
        )
        allPrompts.add(0, newPrompt)
        selectTab(1) // Переключаемся на черновики, чтобы увидеть новый
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PromptSettingsViewModel(context)
            }
        }
    }
}