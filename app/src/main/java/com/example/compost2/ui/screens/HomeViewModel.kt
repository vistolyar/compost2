package com.example.compost2.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.compost2.domain.RecordingItem
import com.example.compost2.domain.RecordingStatus
import java.io.File

class HomeViewModel(private val context: Context) : ViewModel() {

    var recordings by mutableStateOf<List<RecordingItem>>(emptyList())
        private set

    fun loadRecordings() {
        val dir = context.cacheDir
        val files = dir.listFiles { file ->
            file.extension == "m4a" && file.name != "temp_recording.m4a" // Игнорируем временный файл, если запись сорвалась
        } ?: emptyArray()

        val sortedFiles = files.sortedByDescending { it.lastModified() }

        recordings = sortedFiles.map { file ->
            RecordingItem(
                id = file.name,
                name = parseFileNameToDisplay(file.name), // Красивое форматирование
                status = RecordingStatus.SAVED,
                date = "Local", // Дату мы уже вшили в имя, здесь можно упростить
                filePath = file.absolutePath
            )
        }
    }

    // Функция превращает: "2025-12-07-17-41_00-00-39.m4a"
    // В строку: "2025.12.07 17:41 00:00:39"
    private fun parseFileNameToDisplay(fileName: String): String {
        try {
            val nameWithoutExt = fileName.substringBeforeLast(".") // Убираем .m4a

            // Если файл старого формата (просто цифры), возвращаем как есть
            if (!nameWithoutExt.contains("-")) return nameWithoutExt

            // Разбиваем на ДатуВремя и Длительность по символу "_"
            val parts = nameWithoutExt.split("_")
            if (parts.size < 2) return nameWithoutExt

            val dateTimePart = parts[0] // 2025-12-07-17-41
            val durationPart = parts[1] // 00-00-39

            // Форматируем Дату и Время
            // Исходник: yyyy-MM-dd-HH-mm
            val dateComponents = dateTimePart.split("-")
            // dateComponents[0]=yyyy, [1]=MM, [2]=dd, [3]=HH, [4]=mm
            val prettyDate = "${dateComponents[0]}.${dateComponents[1]}.${dateComponents[2]} ${dateComponents[3]}:${dateComponents[4]}"

            // Форматируем Длительность
            // Исходник: HH-mm-ss -> HH:mm:ss
            val prettyDuration = durationPart.replace("-", ":")

            return "$prettyDate   $prettyDuration"
        } catch (e: Exception) {
            // Если что-то пошло не так (странное имя файла), возвращаем оригинал
            return fileName
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(context)
            }
        }
    }
}