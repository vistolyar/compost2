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
            // Если файла нет, создаем дефолтные промпты
            val defaults = listOf(
                PromptItem("1", "Default Transcriber", "Just transcribe the audio exactly as is.", false, "System"),
                PromptItem("2", "Blog Post", "Write a structured blog post based on this transcript.", false, "System"),
                PromptItem("3", "Summary", "Create a bullet-point summary.", false, "System")
            )
            savePrompts(defaults)
            return defaults
        }

        return try {
            val reader = FileReader(file)
            val type = object : TypeToken<List<PromptItem>>() {}.type
            val list: List<PromptItem> = gson.fromJson(reader, type)
            reader.close()
            list
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
        current.add(0, prompt) // Добавляем в начало
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