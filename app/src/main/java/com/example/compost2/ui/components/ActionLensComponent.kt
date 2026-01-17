package com.example.compost2.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.domain.PromptItem
import com.example.compost2.ui.screens.ActionState
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActionLensButton(
    state: ActionState,
    prompts: List<PromptItem>,
    amplitude: Int,
    onResume: () -> Unit,
    onSelect: (PromptItem) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val focusedIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(focusedIndex) {
        if (state == ActionState.SELECTION) {
            val effect = VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Color(0xFF050505))
            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (state == ActionState.SELECTION) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 125.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    LensItem(
                        isResumeButton = true,
                        title = "Resume Recording",
                        subTitle = "(or double tap)",
                        onClick = onResume
                    )
                }
                itemsIndexed(prompts) { _, prompt ->
                    LensItem(
                        isResumeButton = false,
                        title = prompt.title,
                        onClick = { onSelect(prompt) }
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .background(Color.White.copy(alpha = 0.02f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f))
            )
        } else {
            val pulseScale = if (state == ActionState.RECORDING) 1f + (amplitude.toFloat() / 18000f).coerceIn(0f, 0.3f) else 1f
            Box(modifier = Modifier.size(100.dp).scale(pulseScale).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(30.dp).background(if (state == ActionState.RECORDING) Color.Red else Color.White, CircleShape))
            }
        }
    }
}

@Composable
fun LensItem(isResumeButton: Boolean, title: String, subTitle: String? = null, onClick: () -> Unit) {
    val density = LocalDensity.current
    var itemCenterY by remember { mutableStateOf(0f) }
    val viewportCenter = with(density) { 180.dp.toPx() }
    val dist = abs(itemCenterY - viewportCenter)
    val normalizedDist = (dist / 400f).coerceIn(0f, 1f)

    val fontSize = (46 - (normalizedDist * 32)).sp
    val opacity = (1f - (normalizedDist * 0.6f)).coerceIn(0.3f, 1f)
    val weight = if (dist < 60f) FontWeight.Black else FontWeight.Medium

    Box(
        modifier = Modifier
            .fillMaxWidth().height(110.dp).padding(horizontal = 20.dp)
            .onGloballyPositioned { coordinates -> itemCenterY = coordinates.positionInParent().y + (coordinates.size.height / 2f) }
            .padding(vertical = 8.dp)
            .then(if (isResumeButton && dist < 60f) Modifier.border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) else Modifier)
            .clickable { if (dist < 100f) onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ИСПРАВЛЕНИЕ: Используем стиль темы, но переопределяем размер и вес для анимации
            Text(
                text = title,
                color = Color.White,
                fontSize = fontSize,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = weight),
                lineHeight = fontSize * 1.05f,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(opacity)
            )
            if (subTitle != null) {
                Text(
                    text = subTitle,
                    color = Color.White.copy(alpha = opacity * 0.7f),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}