package com.example.compost2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.ui.theme.AppInactive
import com.example.compost2.ui.theme.AppTextPrimary
import com.example.compost2.ui.theme.MontserratFontFamily

@Composable
fun AppTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing...",
    onCopy: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        // Контейнер с рамкой
        Column(
            modifier = Modifier
                .fillMaxSize() // Заполняем переданный размер
                .padding(top = 8.dp)
                .border(1.dp, AppInactive, RoundedCornerShape(12.dp))
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 24.dp) // Top padding для заголовка
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }

            // Текстовое поле с ВНУТРЕННИМ скроллом
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = MontserratFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = AppTextPrimary
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Скролл здесь!
            )
        }

        // ЗАГОЛОВОК (LABEL) - Фиксирован сверху
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AppInactive,
            modifier = Modifier
                .padding(start = 16.dp)
                .background(Color.White)
                .padding(horizontal = 4.dp)
                .align(Alignment.TopStart)
        )

        // КНОПКА КОПИРОВАНИЯ - Фиксирована сверху
        if (onCopy != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 12.dp)
                    .clickable { onCopy() }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = AppInactive,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}