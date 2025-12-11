package com.example.compost2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val OPENAI_KEY = stringPreferencesKey("openai_key")
        val WP_URL = stringPreferencesKey("wp_url")
        val WP_USERNAME = stringPreferencesKey("wp_username")
        val WP_PASSWORD = stringPreferencesKey("wordpress_key")

        // НОВЫЙ КЛЮЧ ДЛЯ ТЕМЫ
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    // --- OpenAI ---
    val openAiKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[OPENAI_KEY] }

    suspend fun saveOpenAiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_KEY] = key
        }
    }

    // --- WordPress ---
    val wpUrl: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[WP_URL] }

    val wpUsername: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[WP_USERNAME] }

    val wpPassword: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[WP_PASSWORD] }

    suspend fun saveWordPressSettings(url: String, username: String, password: String) {
        context.dataStore.edit { preferences ->
            val cleanUrl = if (url.endsWith("/")) url.dropLast(1) else url
            preferences[WP_URL] = cleanUrl
            preferences[WP_USERNAME] = username
            preferences[WP_PASSWORD] = password
        }
    }

    // --- Theme ---
    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[IS_DARK_THEME] }

    suspend fun saveTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }
}