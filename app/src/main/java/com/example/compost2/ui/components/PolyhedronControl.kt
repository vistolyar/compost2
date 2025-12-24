package com.example.compost2.ui.components

import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compost2.domain.PromptItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

val VibrantColors = listOf(
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5),
    Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF009688),
    Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFFFEB3B), Color(0xFFFF9800)
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun PolyhedronControl(
    isRecording: Boolean,
    isPaused: Boolean,
    amplitude: Int,
    state: ControlState,
    prompts: List<PromptItem>,
    onPromptSelected: (PromptItem) -> Unit
) {
    var rx by remember { mutableStateOf(0.45f) }
    var ry by remember { mutableStateOf(0.2f) }
    var rz by remember { mutableStateOf(0.1f) }
    var isAutoRotating by remember { mutableStateOf(true) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(state) {
        if (state == ControlState.SELECTION) {
            isAutoRotating = true
            while (isActive && isAutoRotating) {
                ry += 0.008f; rx += 0.004f; delay(16)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().pointerInput(state) {
        if (state == ControlState.SELECTION) {
            detectDragGestures(onDragStart = { isAutoRotating = false }) { change, dragAmount ->
                change.consume()
                ry += dragAmount.x * 0.005f; rx -= dragAmount.y * 0.005f
            }
        }
    }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            if (state == ControlState.RECORDING) {
                val baseScale = minOf(size.width, size.height) * 0.48f
                val pulse = (amplitude / 32767f) * 80f
                val path = createPentagonPath(center, baseScale + pulse)
                drawPath(path, Color.Red.copy(alpha = if (isRecording) 0.8f else 0.15f), style = Fill)
                drawPath(path, Color.Red, style = Stroke(width = 8.dp.toPx()))
                if (isPaused) {
                    val bw = 24.dp.toPx(); val bh = 70.dp.toPx(); val g = 18.dp.toPx()
                    drawRoundRect(Color.Red, Offset(center.x - bw - g / 2, center.y - bh / 2), Size(bw, bh), CornerRadius(12f))
                    drawRoundRect(Color.Red, Offset(center.x + g / 2, center.y - bh / 2), Size(bw, bh), CornerRadius(12f))
                }
            } else {
                val scale = minOf(size.width, size.height) * 0.75f
                renderDodecahedronWithText(center, scale, rx, ry, rz, prompts, textMeasurer)
            }
        }
    }
}

fun createPentagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    for (i in 0 until 5) {
        val angle = (i * 2 * Math.PI / 5) - Math.PI / 2
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@OptIn(ExperimentalTextApi::class)
fun androidx.compose.ui.graphics.drawscope.DrawScope.renderDodecahedronWithText(
    canvasCenter: Offset, scale: Float, rx: Float, ry: Float, rz: Float, prompts: List<PromptItem>, textMeasurer: TextMeasurer
) {
    val camZ = 5f
    val fov = 2.2f

    val rotatedVerts = DodecahedronGeometry.vertices.map { it.rotX(rx).rotY(ry).rotZ(rz) }
    val projected = rotatedVerts.map { v ->
        val k = fov / (camZ - v.z)
        Offset(canvasCenter.x + v.x * scale * k, canvasCenter.y - v.y * scale * k)
    }

    val faceData = DodecahedronGeometry.faces.indices.map { i ->
        i to DodecahedronGeometry.faces[i].map { rotatedVerts[it].z }.average().toFloat()
    }.sortedBy { it.second }

    faceData.forEach { (fi, avgZ) ->
        val vIdxs = DodecahedronGeometry.faces[fi]
        val path = Path().apply {
            moveTo(projected[vIdxs[0]].x, projected[vIdxs[0]].y)
            vIdxs.drop(1).forEach { lineTo(projected[it].x, projected[it].y) }
            close()
        }

        val isFront = avgZ > 0f
        val brightness = ((avgZ + 1.8f) / 3.6f).coerceIn(0.3f, 1.0f)
        val baseColor = if (fi < prompts.size) VibrantColors[fi % VibrantColors.size] else Color.DarkGray
        val faceColor = Color(baseColor.red * brightness, baseColor.green * brightness, baseColor.blue * brightness)

        drawPath(path, faceColor, style = Fill)
        drawPath(path, Color.White.copy(alpha = if (isFront) 0.4f else 0.1f), style = Stroke(1.dp.toPx()))

        if (isFront && fi < prompts.size) {
            val prompt = prompts[fi]
            val title = prompt.title.take(20) // УБРАН UPPERCASE

            val (c, r, u) = DodecahedronGeometry.faceBasis[fi]
            val tc = c.rotX(rx).rotY(ry).rotZ(rz)
            val tr = (c + r).rotX(rx).rotY(ry).rotZ(rz)
            val tu = (c + u).rotX(rx).rotY(ry).rotZ(rz)

            val kc = fov / (camZ - tc.z); val pc = Offset(canvasCenter.x + tc.x * scale * kc, canvasCenter.y - tc.y * scale * kc)
            val kr = fov / (camZ - tr.z); val pr = Offset(canvasCenter.x + tr.x * scale * kr, canvasCenter.y - tr.y * scale * kr)
            val ku = fov / (camZ - tu.z); val pu = Offset(canvasCenter.x + tu.x * scale * ku, canvasCenter.y - tu.y * scale * ku)

            // Вектора: vx (Вправо), vy (Вниз - инвертируем Up для Canvas)
            val vx = pr - pc
            val vy = pc - pu // Инверсия для корректного Y на Canvas

            var fontSize = 35f
            var textResult: TextLayoutResult
            do {
                textResult = textMeasurer.measure(
                    text = AnnotatedString(title),
                    style = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = fontSize.sp, textAlign = TextAlign.Center)
                )
                fontSize -= 2f
                // УВЕЛИЧЕННЫЙ ОТСТУП (0.35 и 0.25)
            } while ((textResult.size.width > scale * 0.35f || textResult.size.height > scale * 0.25f) && fontSize > 6f)

            drawIntoCanvas { canvas ->
                canvas.save()
                val matrix = Matrix()
                // Исправленная матрица: vx и vy теперь направляют текст правильно
                matrix.setValues(floatArrayOf(
                    vx.x / textResult.size.width * 1.6f, vy.x / textResult.size.height * 1.6f, pc.x,
                    vx.y / textResult.size.width * 1.6f, vy.y / textResult.size.height * 1.6f, pc.y,
                    0f, 0f, 1f
                ))
                canvas.nativeCanvas.concat(matrix)

                drawText(
                    textLayoutResult = textResult,
                    color = Color.White.copy(alpha = brightness.coerceIn(0.8f, 1f)),
                    topLeft = Offset(-textResult.size.width / 2f, -textResult.size.height / 2f)
                )
                canvas.restore()
            }
        }
    }
}