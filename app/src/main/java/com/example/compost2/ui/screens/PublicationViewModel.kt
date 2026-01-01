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
import com.example.compost2.data.auth.GoogleAuthClient
import com.example.compost2.data.auth.GoogleServicesHelper
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
    private val authClient = GoogleAuthClient(context) // Для проверки авторизации

    var recordingItem by mutableStateOf<RecordingItem?>(null)
        private set

    var categories by mutableStateOf<List<WpCategory>>(emptyList())
        private set

    var selectedCategoryIds by mutableStateOf(setOf<Int>())
        private set

    var selectedStatus by mutableStateOf("publish")

    var isLoading by mutableStateOf(false)
    var isPublishing by mutableStateOf(false) // Общий флаг занятости

    var publishedUrl: String? = null
    var publishedId: Int? = null
    var isSuccess by mutableStateOf(false)

    // Флаг, подключен ли Google
    val isGoogleConnected: Boolean
        get() = authClient.getSignedInAccount() != null

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
                    publishedId = item.wordpressId
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
                // Тихая ошибка, если WP не настроен
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

    // --- WORDPRESS PUBLISH ---
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
                    status = selectedStatus,
                    categories = selectedCategoryIds.toList()
                )

                val response = if (publishedId != null) {
                    api.updatePost(publishedId!!, request)
                } else {
                    api.createPost(request)
                }

                publishedUrl = response.link
                publishedId = response.id
                isSuccess = true
                Toast.makeText(context, "Published to WordPress!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "WP Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isPublishing = false
            }
        }
    }

    // --- GOOGLE ACTIONS ---

    private fun getHelper(): GoogleServicesHelper? {
        val account = authClient.getSignedInAccount()
        if (account == null) {
            Toast.makeText(context, "Please sign in with Google first", Toast.LENGTH_SHORT).show()
            return null
        }
        return GoogleServicesHelper(context, account)
    }

    fun exportToCalendar() {
        val item = recordingItem ?: return
        val helper = getHelper() ?: return

        isPublishing = true
        viewModelScope.launch {
            val resultLink = helper.createCalendarEvent(
                title = item.articleTitle ?: "Voice Note",
                description = item.articleContent ?: ""
            )
            isPublishing = false
            if (resultLink != null) {
                Toast.makeText(context, "Event created in Calendar!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to create event", Toast.LENGTH_SHORT).show()
            }
        }
    }

       fun exportToGmail() {
        val item = recordingItem ?: return
        val helper = getHelper() ?: return

        isPublishing = true
        viewModelScope.launch {
            val result = helper.createDraft(
                subject = item.articleTitle ?: "Draft from ComPost",
                bodyText = item.articleContent ?: ""
            )
            isPublishing = false
            if (result != null) {
                Toast.makeText(context, "Draft saved in Gmail!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save draft", Toast.LENGTH_SHORT).show()
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