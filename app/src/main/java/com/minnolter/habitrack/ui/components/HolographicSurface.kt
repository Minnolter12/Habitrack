package com.minnolter.habitrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.minnolter.habitrack.domain.model.ProgressionStage
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Reusable holographic / iridescent material modifier for Habitract cards.
 *
 * Provides a stationary, glassmorphic 3D material shell with clean outer rims,
 * soft inner bevels, and interactive light response on touch.
 * Deliberately static when idle (no continuous animated border movement) to keep 
 * the UI elegant, battery-conscious, and non-distracting.
 */
@Composable
fun Modifier.holographicSurface(
    stage: ProgressionStage,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = LocalReducedMotionPreference.current
): Modifier {
    val coroutineScope = rememberCoroutineScope()

    // Touch position normalized (0f..1f) across the card surface
    val touchX = remember { Animatable(0.3f) }
    val touchY = remember { Animatable(0.2f) }
    val touchIntensity = remember { Animatable(0f) }

    val isMaster = stage == ProgressionStage.MASTER

    // Static ambient light source angle (top-left to bottom-right 135 deg)
    val idleAngleRad = Math.toRadians(-45.0).toFloat()

    return this
        .then(
            if (!reduceMotion) {
                Modifier.graphicsLayer {
                    // Subtle 3D card tilt on touch to simulate physical glass mass
                    val tiltX = (touchY.value - 0.5f) * -3.5f * touchIntensity.value
                    val tiltY = (touchX.value - 0.5f) * 3.5f * touchIntensity.value
                    rotationX = tiltX
                    rotationY = tiltY
                    cameraDistance = 14f * density
                }
            } else {
                Modifier
            }
        )
        .pointerInput(reduceMotion) {
            if (reduceMotion) return@pointerInput

            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val initialX = (down.position.x / size.width).coerceIn(0f, 1f)
                val initialY = (down.position.y / size.height).coerceIn(0f, 1f)

                coroutineScope.launch {
                    touchX.snapTo(initialX)
                    touchY.snapTo(initialY)
                    touchIntensity.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }

                val pointerId = down.id
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointerId }
                    if (change == null || !change.pressed) {
                        break
                    }
                    val currX = (change.position.x / size.width).coerceIn(0f, 1f)
                    val currY = (change.position.y / size.height).coerceIn(0f, 1f)

                    coroutineScope.launch {
                        touchX.snapTo(currX)
                        touchY.snapTo(currY)
                    }
                }

                // Smooth touch release decay back to static resting state
                coroutineScope.launch {
                    touchIntensity.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 800)
                    )
                }
            }
        }
        .drawWithCache {
            val width = size.width
            val height = size.height
            val cornerRadiusPx = 28.dp.toPx()
            val cr = CornerRadius(cornerRadiusPx, cornerRadiusPx)

            if (width <= 0f || height <= 0f) {
                onDrawWithContent { drawContent() }
            } else {
                val palette = getHolographicPalette(stage)
                val blendMode = if (isDarkTheme) BlendMode.Screen else BlendMode.SrcOver

                onDrawWithContent {
                    drawContent()

                    val activeIntensity = touchIntensity.value
                    val isTouched = activeIntensity > 0.01f && !reduceMotion

                    // Smooth transition from static top-left angle to touch angle
                    val currentAngleRad = if (isTouched) {
                        val dx = touchX.value - 0.5f
                        val dy = touchY.value - 0.5f
                        atan2(dy.toDouble(), dx.toDouble()).toFloat()
                    } else {
                        idleAngleRad
                    }

                    val cx = width / 2f
                    val cy = height / 2f
                    val radius = sqrt(cx * cx + cy * cy)

                    val startOffset = Offset(
                        x = (cx - radius * cos(currentAngleRad.toDouble())).toFloat(),
                        y = (cy - radius * sin(currentAngleRad.toDouble())).toFloat()
                    )
                    val endOffset = Offset(
                        x = (cx + radius * cos(currentAngleRad.toDouble())).toFloat(),
                        y = (cy + radius * sin(currentAngleRad.toDouble())).toFloat()
                    )

                    val edgeBrush = Brush.linearGradient(
                        colors = palette.edgeColors,
                        start = startOffset,
                        end = endOffset
                    )

                    // LAYER 1: Ambient Glass Tint Substrate
                    val surfaceAlpha = (if (isDarkTheme) 0.12f else 0.06f) * palette.intensityFactor
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = palette.surfaceColors.map { it.copy(alpha = it.alpha * surfaceAlpha) },
                            start = startOffset,
                            end = endOffset
                        ),
                        cornerRadius = cr,
                        blendMode = blendMode
                    )

                    // LAYER 2: Soft 3D Inner Bevel
                    val bevelWidth = 6.dp.toPx()
                    drawRoundRect(
                        brush = edgeBrush,
                        topLeft = Offset(bevelWidth / 2f, bevelWidth / 2f),
                        size = Size(width - bevelWidth, height - bevelWidth),
                        cornerRadius = CornerRadius(cornerRadiusPx - bevelWidth / 2f, cornerRadiusPx - bevelWidth / 2f),
                        style = Stroke(width = bevelWidth),
                        blendMode = blendMode,
                        alpha = (0.22f + activeIntensity * 0.15f) * palette.intensityFactor
                    )

                    // LAYER 3: Crisp Glass Outer Rim Reflection
                    val rimWidth = 1.25.dp.toPx()
                    drawRoundRect(
                        brush = edgeBrush,
                        topLeft = Offset(rimWidth / 2f, rimWidth / 2f),
                        size = Size(width - rimWidth, height - rimWidth),
                        cornerRadius = CornerRadius(cornerRadiusPx - rimWidth / 2f, cornerRadiusPx - rimWidth / 2f),
                        style = Stroke(width = rimWidth),
                        blendMode = blendMode,
                        alpha = (0.40f + activeIntensity * 0.25f) * palette.intensityFactor
                    )

                    // LAYER 4: Interactive Touch Flare
                    if (isTouched) {
                        val touchCenter = Offset(touchX.value * width, touchY.value * height)
                        val touchRadius = maxOf(width, height) * 0.65f
                        val flareAlpha = activeIntensity * 0.55f

                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = palette.touchColors.map { it.copy(alpha = it.alpha * flareAlpha) },
                                center = touchCenter,
                                radius = touchRadius
                            ),
                            center = touchCenter,
                            radius = touchRadius,
                            blendMode = blendMode
                        )
                    }

                    // Special Prestigious Sheen for Master Tier
                    if (isMaster) {
                        val sweepAlpha = 0.25f * (0.8f + activeIntensity * 0.4f)
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFFFF8E1).copy(alpha = sweepAlpha),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(width, height)
                            ),
                            cornerRadius = cr,
                            blendMode = BlendMode.Screen
                        )
                    }
                }
            }
        }
}

/**
 * Encapsulates the specific color treatments mapped directly from the reference image.
 */
private data class HolographicPalette(
    val edgeColors: List<Color>,
    val surfaceColors: List<Color>,
    val touchColors: List<Color>,
    val intensityFactor: Float
)

private fun getHolographicPalette(stage: ProgressionStage): HolographicPalette {
    return when (stage) {
        ProgressionStage.JUST_STARTED,
        ProgressionStage.BEGINNER,
        ProgressionStage.NOVICE,
        ProgressionStage.DEVELOPING -> {
            HolographicPalette(
                edgeColors = listOf(
                    Color(0xFFFFFFFF), Color(0xFF80DEEA), Color(0x00FFFFFF), Color(0xFFB39DDB), Color(0x60FFFFFF)
                ),
                surfaceColors = listOf(Color(0xFF80DEEA), Color.Transparent, Color(0xFFB39DDB)),
                touchColors = listOf(Color(0xFFFFFFFF), Color(0xFF80DEEA), Color.Transparent),
                intensityFactor = 0.6f
            )
        }

        ProgressionStage.PRACTICING,
        ProgressionStage.COMPETENT,
        ProgressionStage.SKILLED,
        ProgressionStage.PROFICIENT -> {
            HolographicPalette(
                edgeColors = listOf(
                    Color(0xFFFFFFFF), Color(0xFF00E5FF), Color(0x00FFFFFF), Color(0xFF7C4DFF), Color(0x70FFFFFF)
                ),
                surfaceColors = listOf(Color(0xFF00E5FF), Color.Transparent, Color(0xFF7C4DFF)),
                touchColors = listOf(Color(0xFFFFFFFF), Color(0xFF00E5FF), Color.Transparent),
                intensityFactor = 0.85f
            )
        }

        // Advanced tier (Purple / Cyan styling from reference image)
        ProgressionStage.ADVANCED,
        ProgressionStage.EXPERT,
        ProgressionStage.MASTERY,
        ProgressionStage.VIRTUOSO,
        ProgressionStage.ELITE -> {
            HolographicPalette(
                edgeColors = listOf(
                    Color(0xFFFFFFFF), Color(0xFF00FFFF), Color(0xFF7C4DFF), Color(0x00FFFFFF), Color(0xFFFF4081), Color(0x80FFFFFF)
                ),
                surfaceColors = listOf(Color(0xFF00FFFF), Color.Transparent, Color(0xFFFF4081)),
                touchColors = listOf(Color(0xFFFFFFFF), Color(0xFF7C4DFF), Color.Transparent),
                intensityFactor = 1.0f
            )
        }

        // Master tier (Gold / Amber styling from reference image)
        ProgressionStage.MASTER -> {
            HolographicPalette(
                edgeColors = listOf(
                    Color(0xFFFFFFFF), Color(0xFFFFD700), Color(0x00FFFFFF), Color(0xFFFF8F00), Color(0x90FFFFFF)
                ),
                surfaceColors = listOf(Color(0xFFFFD700), Color.Transparent, Color(0xFFFF8F00)),
                touchColors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD700), Color.Transparent),
                intensityFactor = 1.1f
            )
        }
    }
}