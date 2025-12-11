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

    // Список рубрик с сайта
    var categories by mutableStateOf<List<WpCategory>>(emptyList())
        private set

    // Выбранные рубрики (ID)
    var selectedCategoryIds by mutableStateOf(setOf<Int>())
        private set

    var isLoading by mutableStateOf(false)
    var isPublishing by mutableStateOf(false)

    // Ссылка на опубликованную статью
    var publishedUrl: String? = null
    var isSuccess by mutableStateOf(false)

    fun loadData(fileName: String) {
        // 1. Загружаем данные статьи из локального JSON
        val file = File(context.cacheDir, fileName)
        if (file.exists()) {
            val metaFile = File(file.path + ".json")
            if (metaFile.exists()) {
                try {
                    val reader = FileReader(metaFile)
                    val item = gson.fromJson(reader, RecordingItem::class.java)
                    reader.close()
                    recordingItem = item.copy(filePath = file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // 2. Загружаем рубрики с сайта
        fetchCategories()
    }

    private fun fetchCategories() {
        isLoading = true
        viewModelScope.launch {
            try {
                // Читаем настройки
                val url = dataStore.wpUrl.first() ?: ""
                val username = dataStore.wpUsername.first() ?: ""
                val password = dataStore.wpPassword.first() ?: ""

                if (url.isNotBlank() && password.isNotBlank()) {
                    val api = WpClient.create(url, username, password)
                    categories = api.getCategories()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load categories: ${e.message}", Toast.LENGTH_LONG).show()
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

    fun publishPost(status: String = "publish") { // "publish" или "draft"
        val item = recordingItem ?: return
        if (isPublishing) return

        isPublishing = true
        viewModelScope.launch {
            try {
                val url = dataStore.wpUrl.first() ?: ""
                val username = dataStore.wpUsername.first() ?: ""
                val password = dataStore.wpPassword.first() ?: ""

                if (url.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "WP Settings missing!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val api = WpClient.create(url, username, password)

                val request = WpPostRequest(
                    title = item.articleTitle ?: "New Article",
                    content = item.articleContent ?: "",
                    status = status,
                    categories = selectedCategoryIds.toList()
                )

                val response = api.createPost(request)

                publishedUrl = response.link
                isSuccess = true
                Toast.makeText(context, "Success! Post ID: ${response.id}", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Publish Error: ${e.message}", Toast.LENGTH_LONG).show()
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