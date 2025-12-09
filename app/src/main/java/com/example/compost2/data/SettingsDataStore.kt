package com.example.compost2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Расширение для создания единственного экземпляра DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val OPENAI_KEY = stringPreferencesKey("openai_key")
        val WORDPRESS_KEY = stringPreferencesKey("wordpress_key")
    }

    // Чтение (Flow - поток данных, обновляется сам при изменении)
    val openAiKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[OPENAI_KEY] }

    val wordPressKey: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[WORDPRESS_KEY] }

    // Запись
    suspend fun saveOpenAiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_KEY] = key
        }
    }

    suspend fun saveWordPressKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[WORDPRESS_KEY] = key
        }
    }
}