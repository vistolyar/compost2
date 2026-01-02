package com.example.compost2.data

import android.content.Context
import com.example.compost2.domain.IntegrationType
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
            return createDefaults()
        }

        return try {
            val reader = FileReader(file)
            val type = object : TypeToken<List<PromptItem>>() {}.type
            val list: List<PromptItem>? = gson.fromJson(reader, type)
            reader.close()

            if (list == null) return createDefaults()

            // --- ПРОВЕРКА НА БИТЫЕ/СТАРЫЕ ДАННЫЕ ---
            // Если хотя бы у одного элемента поле integrationType равно null (из-за Gson),
            // считаем файл устаревшим и пересоздаем его.
            val hasInvalidItems = list.any { it.integrationType == null }

            if (hasInvalidItems) {
                // Можно попытаться мигрировать данные, но проще пересоздать дефолтные,
                // так как это development-версия.
                file.delete()
                return createDefaults()
            }

            list
        } catch (e: Exception) {
            e.printStackTrace()
            // В случае любой ошибки чтения (например, изменилась структура) - сброс
            file.delete()
            createDefaults()
        }
    }

    private fun createDefaults(): List<PromptItem> {
        val defaults = listOf(
            PromptItem(
                id = "1",
                title = "Default Transcriber",
                content = "Just transcribe the audio exactly as is.",
                integrationType = IntegrationType.NONE,
                isDraft = false,
                lastModified = "System"
            ),
            PromptItem(
                id = "2",
                title = "Blog Post",
                content = "Write a structured blog post based on this transcript.",
                integrationType = IntegrationType.WORDPRESS,
                isDraft = false,
                lastModified = "System"
            ),
            PromptItem(
                id = "3",
                title = "Meeting Summary",
                content = "Create a bullet-point summary and extract action items.",
                integrationType = IntegrationType.NONE,
                isDraft = false,
                lastModified = "System"
            ),
            PromptItem(
                id = "4",
                title = "Create Event",
                content = "Extract date, time and title for a calendar event.",
                integrationType = IntegrationType.CALENDAR,
                isDraft = false,
                lastModified = "System"
            )
        )
        savePrompts(defaults)
        return defaults
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
        current.add(0, prompt)
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