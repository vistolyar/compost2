package com.example.compost2.data

import android.content.Context
import com.example.compost2.domain.PromptItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PromptsRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "prompts.json"
    private val file = File(context.filesDir, fileName)

    fun getPrompts(): List<PromptItem> {
        if (!file.exists()) {
            val defaults = listOf(
                PromptItem("1", "Default Transcriber", "Just transcribe exactly.", false, "System", 0),
                PromptItem("2", "Jira Task", "Create a structured Jira task.", false, "System", 1),
                PromptItem("3", "Email to Boss", "Write a formal email.", false, "System", 2),
                PromptItem("4", "Summary", "Make a short summary.", false, "System", 3),
                PromptItem("5", "Blog Post", "Write a WordPress post.", false, "System", 4),
                PromptItem("6", "LinkedIn", "Punchy post with emojis.", false, "System", 5),
                PromptItem("7", "Action Items", "Extract a to-do list.", false, "System", 6),
                PromptItem("8", "Grammar Fix", "Just fix mistakes.", false, "System", 7),
                PromptItem("9", "Code helper", "Extract code or logic.", false, "System", 8),
                PromptItem("10", "Translate", "Translate to English.", false, "System", 9),
                PromptItem("11", "Ideas", "Brainstorm 5 more ideas.", false, "System", 10),
                PromptItem("12", "Transcription", "Raw text only.", false, "System", 11)
            )
            savePrompts(defaults)
            return defaults
        }

        return try {
            val reader = FileReader(file)
            val type = object : TypeToken<List<PromptItem>>() {}.type
            val list: List<PromptItem> = gson.fromJson(reader, type)
            reader.close()
            // СОРТИРОВКА ПО ПОЗИЦИИ
            list.sortedBy { it.position }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun savePrompts(prompts: List<PromptItem>) {
        try {
            val writer = FileWriter(file)
            gson.toJson(prompts, writer)
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addPrompt(prompt: PromptItem) {
        val current = getPrompts().toMutableList()
        current.add(prompt)
        savePrompts(current)
    }

    fun deletePrompt(id: String) {
        val current = getPrompts().toMutableList()
        current.removeIf { it.id == id }
        savePrompts(current)
    }

    fun updatePrompt(updatedPrompt: PromptItem) {
        val current = getPrompts().toMutableList()
        val index = current.indexOfFirst { it.id == updatedPrompt.id }
        if (index != -1) {
            current[index] = updatedPrompt
            savePrompts(current)
        }
    }
}