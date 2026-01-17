package com.example.compost2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing...",
    showJsonTab: Boolean = false, // Показывать ли вторую вкладку
    onCopy: () -> Unit // Действие копирования
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppInactive.copy(alpha = 0.3f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
    ) {
        // --- HEADER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Вкладки
            Row(
                modifier = Modifier.padding(start = 25.dp, top = 15.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "PROMPT TEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTextPrimary
                )

                if (showJsonTab) {
                    Text(
                        text = "JSON PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppTextPrimary.copy(alpha = 0.3f)
                    )
                }
            }

            // Тулбар (Только кнопка копирования справа)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8FA))
                    .border(width = 1.dp, color = AppInactive.copy(alpha = 0.2f))
                    .padding(horizontal = 25.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End, // Всё вправо
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка копирования
                Box(
                    modifier = Modifier
                        .clickable { onCopy() }
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- TEXT AREA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(25.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = MontserratFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = AppTextPrimary
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}