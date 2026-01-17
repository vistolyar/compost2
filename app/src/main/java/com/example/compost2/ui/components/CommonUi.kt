package com.example.compost2.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.ui.theme.*

// 1. ЕДИНЫЙ ФОН ЭКРАНА
@Composable
fun AppScreenContainer(
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppScreenBg),
        content = content
    )
}

// 2. УНИВЕРСАЛЬНАЯ ШАПКА (AppTopBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = AppTextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AppTextPrimary
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppScreenBg,
            scrolledContainerColor = AppScreenBg
        ),
        windowInsets = WindowInsets.statusBars
    )
}

// 3. УНИФИЦИРОВАННАЯ КАРТОЧКА
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppCard(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (isSelected) AppPrimary else Color.Transparent
    val borderWidth = if (isSelected) 3.dp else 0.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppCardBg,
            contentColor = AppTextPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// 4. ИНДИКАТОР СТАТУСА
@Composable
fun StatusIndicatorDot(isActive: Boolean) {
    val color = if (isActive) AppSuccess else AppWarning
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape)
    )
}

// 5. КНОПКИ УПРАВЛЕНИЯ
@Composable
fun AppActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    isEnabled: Boolean = true,
    statusDotColor: Color? = null
) {
    val containerColor = if (isDestructive) AppDanger else AppPrimary
    val finalContainerColor = if (isEnabled) containerColor else AppInactive

    Button(
        onClick = onClick,
        // Ограничиваем минимальную высоту, но НЕ максимальную.
        modifier = modifier.heightIn(min = 48.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = finalContainerColor,
            contentColor = Color.White,
            disabledContainerColor = AppInactive,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        // Убираем паддинги самой кнопки, чтобы Box мог растянуться для позиционирования точки
        contentPadding = PaddingValues(0.dp)
    ) {
        // ИСПРАВЛЕНИЕ: Убрали fillMaxHeight(), чтобы кнопка не растягивала весь экран
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Текст кнопки с отступами
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
            )

            // Лампочка (Привязана к правому верхнему углу контейнера кнопки)
            if (statusDotColor != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Правый верхний угол
                        .padding(top = 6.dp, end = 6.dp) // Небольшой отступ от края
                        .size(8.dp)
                        .background(statusDotColor, CircleShape)
                )
            }
        }
    }
}

@Composable
fun AppGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = AppTextPrimary
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            fontSize = 13.sp
        )
    }
}