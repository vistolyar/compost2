package com.example.compost2.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.data.network.WpCategory
import com.example.compost2.data.network.WpClient
import com.example.compost2.data.network.WpPostRequest
import com.example.compost2.domain.RecordingItem
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader

class PublicationViewModel(private val context: Context) : ViewModel() {

    private val dataStore = SettingsDataStore(context)
    private val gson = Gson()

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    var categories by mutableStateOf<List<WpCategory>>(emptyList())
        private set

    var selectedCategoryIds by mutableStateOf(setOf<Int>())
        private set

    // Выбранный статус публикации
    // Варианты WP: 'publish', 'draft', 'private', 'pending'
    var selectedStatus by mutableStateOf("publish")

    var isLoading by mutableStateOf(false)
    var isPublishing by mutableStateOf(false)

    var publishedUrl: String? = null
    var publishedId: Int? = null // ID опубликованного поста
    var isSuccess by mutableStateOf(false)

    fun loadData(fileName: String) {
        val file = File(context.cacheDir, fileName)
        if (file.exists()) {
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    val reader = FileReader(metaFile)
                    val item = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()
                    recordingItem = item.copy(filePath = file.absolutePath)

                    // Если пост уже был опубликован, выставляем его ID
                    publishedId = item.wordpressId

                    // Если мы пришли редактировать, статус по умолчанию берем из логики?
                    // Пока оставим по умолчанию 'publish', но можно усложнить
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        fetchCategories()
    }

    private fun fetchCategories() {
        isLoading = true
        viewModelScope.launch {
            try {
                val url = dataStore.wpUrl.first() ?: ""
                val username = dataStore.wpUsername.first() ?: ""
                val password = dataStore.wpPassword.first() ?: ""

                if (url.isNotBlank() && password.isNotBlank()) {
                    val api = WpClient.create(url, username, password)
                    categories = api.getCategories()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleCategory(categoryId: Int) {
        selectedCategoryIds = if (selectedCategoryIds.contains(categoryId)) {
            selectedCategoryIds - categoryId
        } else {
            selectedCategoryIds + categoryId
        }
    }

    fun setStatus(status: String) {
        selectedStatus = status
    }

    // Главный метод: Создать или Обновить
    fun submitPost() {
        val item = recordingItem ?: return
        if (isPublishing) return

        isPublishing = true
        viewModelScope.launch {
            try {
                val url = dataStore.wpUrl.first() ?: ""
                val username = dataStore.wpUsername.first() ?: ""
                val password = dataStore.wpPassword.first() ?: ""

                if (url.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Check WP Settings!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val api = WpClient.create(url, username, password)

                val request = WpPostRequest(
                    title = item.articleTitle ?: "Untitled",
                    content = item.articleContent ?: "",
                    status = selectedStatus, // Используем выбранный статус
                    categories = selectedCategoryIds.toList()
                )

                // ЛОГИКА ВЫБОРА: CREATE vs UPDATE
                val response = if (publishedId != null) {
                    // Если ID есть - обновляем
                    api.updatePost(publishedId!!, request)
                } else {
                    // Если ID нет - создаем
                    api.createPost(request)
                }

                publishedUrl = response.link
                publishedId = response.id // Обновляем ID (на случай если это было создание)

                isSuccess = true
                val action = if (publishedId == item.wordpressId) "Updated" else "Published"
                Toast.makeText(context, "$action successfully!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isPublishing = false
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PublicationViewModel(context)
            }
        }
    }
}