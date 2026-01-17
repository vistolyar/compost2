package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.data.SettingsDataStore
import com.example.compost2.domain.IntegrationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IntegrationsViewModel(private val dataStore: SettingsDataStore) : ViewModel() {

    // Храним состояние включенности (true/false) для каждого типа
    private val _enabledIntegrations = mutableStateMapOf<IntegrationType, Boolean>()
    val enabledIntegrations: Map<IntegrationType, Boolean> get() = _enabledIntegrations

    init {
        // Инициализация. В реальном app грузим из DataStore.
        // Пока ставим дефолты: Text всегда вкл, остальные тоже вкл по умолчанию
        IntegrationType.values().forEach { type ->
            _enabledIntegrations[type] = true
        }

        // Загрузка реальных настроек (симуляция загрузки)
        viewModelScope.launch {
            // Здесь можно добавить чтение из DataStore в будущем
            // dataStore.getIntegrationState(type)
        }
    }

    fun toggleIntegration(type: IntegrationType) {
        if (type == IntegrationType.NONE) return // Текстовую нельзя выключить

        val current = _enabledIntegrations[type] ?: true
        _enabledIntegrations[type] = !current

        // Сохранение
        saveState(type, !current)
    }

    private fun saveState(type: IntegrationType, isEnabled: Boolean) {
        viewModelScope.launch {
            // dataStore.saveIntegrationState(type, isEnabled)
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                IntegrationsViewModel(SettingsDataStore(context))
            }
        }
    }
}