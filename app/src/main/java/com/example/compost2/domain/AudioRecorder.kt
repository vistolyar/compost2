package com.example.compost2.domain

import java.io.File

interface AudioRecorder {
    // Начать новую запись в файл
    fun start(outputFile: File)

    // Поставить на паузу (не закрывая файл)
    fun pause()

    // Продолжить запись в тот же файл
    fun resume()

    // Закончить и сохранить
    fun stop()

    // Получить текущую громкость (для анимации кнопки)
    // Возвращает число от 0 до 32767
    fun getAmplitude(): Int
}