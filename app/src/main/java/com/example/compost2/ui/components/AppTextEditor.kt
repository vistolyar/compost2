package com.example.compost2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
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
import com.example.compost2.ui.theme.MontserratFontFamily

@Composable
fun AppTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing..."
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFF0F0F2), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
    ) {
        // --- STICKY HEADER STYLE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Вкладки (Декоративные)
            Row(
                modifier = Modifier.padding(start = 25.dp, top = 15.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "PROMPT TEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
                Text(
                    text = "JSON PREVIEW", // Для промптов это логичнее, чем HTML
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black.copy(alpha = 0.3f)
                )
            }

            // Тулбар
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8FA))
                    .border(width = 1.dp, color = Color(0xFFF0F0F2))
                    .padding(horizontal = 25.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FormatListBulleted, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                Icon(Icons.Default.FormatBold, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                Icon(Icons.Default.FormatItalic, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ContentCopy, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
            }
        }

        // --- TEXT AREA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Занимает всё оставшееся место
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
                    color = Color.Black
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}