package com.programobil.eclipse_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.programobil.eclipse_frontend.ui.theme.EclipseFrontEndTheme
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sqrt
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EclipseFrontEndTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val boxSize = 0.95f
    var sunPosition by remember { mutableStateOf(Offset(0.8f, 0.2f)) }
    // Animación suave para x e y
    val animatedX by animateFloatAsState(targetValue = sunPosition.x, animationSpec = tween(durationMillis = 400))
    val animatedY by animateFloatAsState(targetValue = sunPosition.y, animationSpec = tween(durationMillis = 400))
    val animatedSunPosition = Offset(animatedX, animatedY)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
        Canvas(
            modifier = Modifier
                .fillMaxSize(boxSize)
                .onGloballyPositioned { layoutCoordinates ->
                    val size = layoutCoordinates.size
                    canvasSize = androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat())
                }
                .pointerInput(Unit) {
                    while (true) {
                        val event = awaitPointerEventScope { awaitPointerEvent() }
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val size = this.size
                            val tap = change.position
                            val newX = (tap.x / size.width).coerceIn(0f, 1f)
                            val newY = (tap.y / size.height).coerceIn(0f, 1f)
                            sunPosition = Offset(newX, newY)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val lensRadius = width * 0.18f
            val lensY = height * 0.5f
            val leftLensCenter = Offset(width * 0.32f, lensY)
            val rightLensCenter = Offset(width * 0.68f, lensY)
            val bridgeStart = Offset(leftLensCenter.x + lensRadius * 0.85f, lensY)
            val bridgeEnd = Offset(rightLensCenter.x - lensRadius * 0.85f, lensY)
            val sunPx = Offset(animatedSunPosition.x * width, animatedSunPosition.y * height)
            val sunRadius = width * 0.08f
            val lightDir = (Offset((leftLensCenter.x + rightLensCenter.x) / 2, lensY) - sunPx).normalizeCompat()
            val shadowLength = (width * 0.18f) + (width * 0.25f * (1.2f - animatedSunPosition.y))
            val shadowAlpha = 0.32f
            val shadowOffset = lightDir * shadowLength
            val shadowColor = Color.Black.copy(alpha = shadowAlpha)
            // Sombra izquierda
            drawOval(
                color = shadowColor,
                topLeft = Offset(
                    leftLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                    leftLensCenter.y + shadowOffset.y - lensRadius * 0.7f
                ),
                size = Size(lensRadius * 2.2f, lensRadius * 1.4f)
            )
            // Sombra derecha
            drawOval(
                color = shadowColor,
                topLeft = Offset(
                    rightLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                    rightLensCenter.y + shadowOffset.y - lensRadius * 0.7f
                ),
                size = Size(lensRadius * 2.2f, lensRadius * 1.4f)
            )
            // Sombra del puente
            drawOval(
                color = shadowColor,
                topLeft = Offset(
                    (bridgeStart.x + bridgeEnd.x) / 2 + shadowOffset.x - lensRadius * 0.4f,
                    lensY + shadowOffset.y - lensRadius * 0.18f
                ),
                size = Size(lensRadius * 0.8f, lensRadius * 0.36f)
            )
            // Lentes
            drawCircle(
                color = Color.Black,
                center = leftLensCenter,
                radius = lensRadius,
                style = Stroke(width = width * 0.025f)
            )
            drawCircle(
                color = Color.Black,
                center = rightLensCenter,
                radius = lensRadius,
                style = Stroke(width = width * 0.025f)
            )
            // Puente
            drawLine(
                color = Color.Black,
                start = bridgeStart,
                end = bridgeEnd,
                strokeWidth = width * 0.06f
            )
            // Patillas
            drawLine(
                color = Color.Black,
                start = Offset(leftLensCenter.x - lensRadius, lensY),
                end = Offset(leftLensCenter.x - lensRadius * 1.4f, lensY - lensRadius * 0.3f),
                strokeWidth = width * 0.018f
            )
            drawLine(
                color = Color.Black,
                start = Offset(rightLensCenter.x + lensRadius, lensY),
                end = Offset(rightLensCenter.x + lensRadius * 1.4f, lensY - lensRadius * 0.3f),
                strokeWidth = width * 0.018f
            )
            // Reflejo en los lentes
            val reflectionOffset = (sunPx - leftLensCenter).normalizeCompat() * lensRadius * 0.5f
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                center = leftLensCenter + reflectionOffset,
                radius = lensRadius * 0.22f
            )
            val reflectionOffset2 = (sunPx - rightLensCenter).normalizeCompat() * lensRadius * 0.5f
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                center = rightLensCenter + reflectionOffset2,
                radius = lensRadius * 0.22f
            )
            // Sol
            drawCircle(
                color = Color(0xFFFFEB3B),
                center = sunPx,
                radius = sunRadius,
                style = Stroke(width = width * 0.01f)
            )
            drawCircle(
                color = Color(0xFFFFF176),
                center = sunPx,
                radius = sunRadius * 0.7f
            )
            // --- Bounding boxes cuadradas ---
            val boxColor = Color(0xFF2196F3).copy(alpha = 0.18f)
            // Sol
            val solRect = androidx.compose.ui.geometry.Rect(
                sunPx.x - sunRadius,
                sunPx.y - sunRadius,
                sunPx.x + sunRadius,
                sunPx.y + sunRadius
            )
            drawRect(
                color = boxColor,
                topLeft = solRect.topLeft,
                size = solRect.size,
                style = Stroke(width = width * 0.008f)
            )
            // Lentes
            val leftLensRect = androidx.compose.ui.geometry.Rect(
                leftLensCenter.x - lensRadius,
                leftLensCenter.y - lensRadius,
                leftLensCenter.x + lensRadius,
                leftLensCenter.y + lensRadius
            )
            val rightLensRect = androidx.compose.ui.geometry.Rect(
                rightLensCenter.x - lensRadius,
                rightLensCenter.y - lensRadius,
                rightLensCenter.x + lensRadius,
                rightLensCenter.y + lensRadius
            )
            drawRect(
                color = boxColor,
                topLeft = leftLensRect.topLeft,
                size = leftLensRect.size,
                style = Stroke(width = width * 0.008f)
            )
            drawRect(
                color = boxColor,
                topLeft = rightLensRect.topLeft,
                size = rightLensRect.size,
                style = Stroke(width = width * 0.008f)
            )
            // Sombras
            val leftShadowRect = androidx.compose.ui.geometry.Rect(
                leftLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                leftLensCenter.y + shadowOffset.y - lensRadius * 0.7f,
                leftLensCenter.x + shadowOffset.x + lensRadius * 1.1f,
                leftLensCenter.y + shadowOffset.y + lensRadius * 0.7f
            )
            val rightShadowRect = androidx.compose.ui.geometry.Rect(
                rightLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                rightLensCenter.y + shadowOffset.y - lensRadius * 0.7f,
                rightLensCenter.x + shadowOffset.x + lensRadius * 1.1f,
                rightLensCenter.y + shadowOffset.y + lensRadius * 0.7f
            )
            val bridgeShadowRect = androidx.compose.ui.geometry.Rect(
                (bridgeStart.x + bridgeEnd.x) / 2 + shadowOffset.x - lensRadius * 0.4f,
                lensY + shadowOffset.y - lensRadius * 0.18f,
                (bridgeStart.x + bridgeEnd.x) / 2 + shadowOffset.x + lensRadius * 0.4f,
                lensY + shadowOffset.y + lensRadius * 0.18f
            )
            drawRect(
                color = boxColor,
                topLeft = leftShadowRect.topLeft,
                size = leftShadowRect.size,
                style = Stroke(width = width * 0.008f)
            )
            drawRect(
                color = boxColor,
                topLeft = rightShadowRect.topLeft,
                size = rightShadowRect.size,
                style = Stroke(width = width * 0.008f)
            )
            drawRect(
                color = boxColor,
                topLeft = bridgeShadowRect.topLeft,
                size = bridgeShadowRect.size,
                style = Stroke(width = width * 0.008f)
            )
            // --- Fin bounding boxes ---
        }
        // Etiquetas sobre el Canvas
        if (canvasSize != androidx.compose.ui.geometry.Size.Zero) {
            val width = canvasSize.width
            val height = canvasSize.height
            val lensRadius = width * 0.18f
            val lensY = height * 0.5f
            val leftLensCenter = Offset(width * 0.32f, lensY)
            val rightLensCenter = Offset(width * 0.68f, lensY)
            val sunPx = Offset(animatedSunPosition.x * width, animatedSunPosition.y * height)
            val sunRadius = width * 0.08f
            val bridgeStart = Offset(leftLensCenter.x + lensRadius * 0.85f, lensY)
            val bridgeEnd = Offset(rightLensCenter.x - lensRadius * 0.85f, lensY)
            val lightDir = (Offset((leftLensCenter.x + rightLensCenter.x) / 2, lensY) - sunPx).normalizeCompat()
            val shadowLength = (width * 0.18f) + (width * 0.25f * (1.2f - animatedSunPosition.y))
            val shadowOffset = lightDir * shadowLength
            // Sol
            val solRect = androidx.compose.ui.geometry.Rect(
                sunPx.x - sunRadius,
                sunPx.y - sunRadius,
                sunPx.x + sunRadius,
                sunPx.y + sunRadius
            )
            Text(
                text = "sol",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (solRect.left - 8f).toInt(),
                        y = (solRect.top - 24f).toInt()
                    )
                }
            )
            // Lentes
            val leftLensRect = androidx.compose.ui.geometry.Rect(
                leftLensCenter.x - lensRadius,
                leftLensCenter.y - lensRadius,
                leftLensCenter.x + lensRadius,
                leftLensCenter.y + lensRadius
            )
            val rightLensRect = androidx.compose.ui.geometry.Rect(
                rightLensCenter.x - lensRadius,
                rightLensCenter.y - lensRadius,
                rightLensCenter.x + lensRadius,
                rightLensCenter.y + lensRadius
            )
            Text(
                text = "lente",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (leftLensRect.left - 8f).toInt(),
                        y = (leftLensRect.top - 24f).toInt()
                    )
                }
            )
            Text(
                text = "lente",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (rightLensRect.left - 8f).toInt(),
                        y = (rightLensRect.top - 24f).toInt()
                    )
                }
            )
            // Sombras
            val leftShadowRect = androidx.compose.ui.geometry.Rect(
                leftLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                leftLensCenter.y + shadowOffset.y - lensRadius * 0.7f,
                leftLensCenter.x + shadowOffset.x + lensRadius * 1.1f,
                leftLensCenter.y + shadowOffset.y + lensRadius * 0.7f
            )
            val rightShadowRect = androidx.compose.ui.geometry.Rect(
                rightLensCenter.x + shadowOffset.x - lensRadius * 1.1f,
                rightLensCenter.y + shadowOffset.y - lensRadius * 0.7f,
                rightLensCenter.x + shadowOffset.x + lensRadius * 1.1f,
                rightLensCenter.y + shadowOffset.y + lensRadius * 0.7f
            )
            val bridgeShadowRect = androidx.compose.ui.geometry.Rect(
                (bridgeStart.x + bridgeEnd.x) / 2 + shadowOffset.x - lensRadius * 0.4f,
                lensY + shadowOffset.y - lensRadius * 0.18f,
                (bridgeStart.x + bridgeEnd.x) / 2 + shadowOffset.x + lensRadius * 0.4f,
                lensY + shadowOffset.y + lensRadius * 0.18f
            )
            Text(
                text = "sombra",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (leftShadowRect.left - 8f).toInt(),
                        y = (leftShadowRect.top - 24f).toInt()
                    )
                }
            )
            Text(
                text = "sombra",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (rightShadowRect.left - 8f).toInt(),
                        y = (rightShadowRect.top - 24f).toInt()
                    )
                }
            )
            Text(
                text = "sombra",
                color = Color(0xFF444444).copy(alpha = 0.35f),
                fontSize = 12.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (bridgeShadowRect.left - 8f).toInt(),
                        y = (bridgeShadowRect.top - 24f).toInt()
                    )
                }
            )
        }
    }
}

private fun Offset.normalizeCompat(): Offset {
    val len = sqrt(x * x + y * y)
    return if (len == 0f) this else Offset(x / len, y / len)
}
private fun Offset.getDistanceCompat(): Float {
    return sqrt(x * x + y * y)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EclipseFrontEndTheme {
        Greeting()
    }
}