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
            val center = Offset(width / 2, height / 2)
            val sphereRadius = width * 0.18f
            val sunPx = Offset(animatedSunPosition.x * width, animatedSunPosition.y * height)
            val sunRadius = width * 0.08f
            val lightDir = (sunPx - center).normalizeCompat()

            // Fondo con gradiente vertical estilo Eclipse de Berserk
            for (i in 0..height.toInt() step 4) {
                val frac = i / height
                drawRect(
                    color = lerpColor(Color(0xFF3B0000), Color(0xFFFF2D2D), frac),
                    topLeft = Offset(0f, i.toFloat()),
                    size = Size(width, 4f)
                )
            }

            // Esfera con gradiente radial homogéneo (centro claro, borde oscuro)
            val gradSteps = 80
            for (i in gradSteps downTo 1) {
                val frac = i / gradSteps.toFloat()
                val color = when {
                    frac > 0.85f -> lerpColor(Color.White, Color(0xFFFFD600), (frac - 0.85f) / 0.15f) // blanco a amarillo
                    frac > 0.6f -> lerpColor(Color(0xFFFFD600), Color(0xFFFF9800), (frac - 0.6f) / 0.25f) // amarillo a naranja
                    frac > 0.3f -> lerpColor(Color(0xFFFF9800), Color(0xFF8B0000), (frac - 0.3f) / 0.3f) // naranja a rojo oscuro
                    else -> lerpColor(Color(0xFF8B0000), Color.Black, (0.3f - frac) / 0.3f) // rojo oscuro a negro
                }
                val offset = lightDir * sphereRadius * 0.18f * (1 - frac)
                drawCircle(
                    color = color,
                    center = center + offset,
                    radius = sphereRadius * frac
                )
            }
            // Reflejo especular pequeño y suave
            val highlightOffset = lightDir * sphereRadius * 0.55f
            drawOval(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(
                    (center + highlightOffset).x - sphereRadius * 0.09f,
                    (center + highlightOffset).y - sphereRadius * 0.045f
                ),
                size = Size(sphereRadius * 0.18f, sphereRadius * 0.09f)
            )

            // Eclipse de Berserk simplificado
            val eclipseRadius = sunRadius * 1.5f
            // Halo rojo difuso
            for (i in 5 downTo 1) {
                val frac = i / 5f
                drawCircle(
                    color = Color(0xFFFF2D2D).copy(alpha = 0.10f * frac),
                    center = sunPx,
                    radius = eclipseRadius * (1f + frac * 0.7f)
                )
            }
            // Borde rojo más delgado
            drawCircle(
                color = Color(0xFFFF2D2D),
                center = sunPx,
                radius = eclipseRadius,
                style = Stroke(width = width * 0.018f)
            )
            // Centro negro profundo
            drawCircle(
                color = Color.Black,
                center = sunPx,
                radius = eclipseRadius * 0.97f
            )

            // Sombra proyectada (más realista: más alargada y difusa cuando el eclipse está bajo)
            val shadowDir = (center - sunPx).normalizeCompat()
            val eclipseHeight = (sunPx.y - center.y) / sphereRadius // negativo: arriba, positivo: abajo
            val shadowDist = sphereRadius * (1.2f + 0.7f * eclipseHeight.coerceIn(-1f, 1f))
            val shadowCenter = center + shadowDir * shadowDist
            val shadowWidth = sphereRadius * (1.1f + 0.5f * eclipseHeight.coerceIn(-1f, 1f))
            val shadowHeight = sphereRadius * (0.45f - 0.18f * eclipseHeight.coerceIn(-1f, 1f))
            val shadowAlphaBase = (0.22f - 0.10f * eclipseHeight.coerceIn(-1f, 1f)).coerceIn(0.08f, 0.25f)
            for (i in 0..4) {
                val alpha = shadowAlphaBase / (i + 1)
                val scale = 1f + i * 0.18f
                drawOval(
                    color = Color.Black.copy(alpha = alpha),
                    topLeft = Offset(
                        shadowCenter.x - shadowWidth * scale,
                        shadowCenter.y - (shadowHeight * scale) / 2 + sphereRadius * 1.1f
                    ),
                    size = Size(shadowWidth * 2 * scale, shadowHeight * scale)
                )
            }
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

// Utilidad para interpolar colores
fun lerpColor(a: Color, b: Color, t: Float): Color {
    return Color(
        red = a.red + (b.red - a.red) * t,
        green = a.green + (b.green - a.green) * t,
        blue = a.blue + (b.blue - a.blue) * t,
        alpha = a.alpha + (b.alpha - a.alpha) * t
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EclipseFrontEndTheme {
        Greeting()
    }
}