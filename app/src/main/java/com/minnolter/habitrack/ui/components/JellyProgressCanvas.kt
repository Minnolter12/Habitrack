package com.minnolter.habitrack.ui.components

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * The signature Habitract visualization (Section 17): a translucent liquid
 * that fills a card **left to right** as [visualProgress] increases, bounded
 * by an organic, gently animating squiggly edge rather than a hard vertical
 * line.
 *
 * [visualProgress] is expected to already be the nonlinear, card-friendly
 * value from `ProgressionStage.calculateVisualProgress` (0f..1f) — this
 * composable only concerns itself with rendering, never with what the
 * number honestly means.
 *
 * [isMasterStage] triggers the Section 23 special state: the fill is forced
 * to full width, the wave is calmed to a gentle shimmer rather than an
 * energetic squiggle, and a soft, continuous golden sweep plays across the
 * card. This is the ambient "prestigious" resting state — a distinct
 * one-shot celebration animation for the moment a milestone is first
 * reached (Section 29) belongs to a later, event-driven overlay and is out
 * of scope for this stateless canvas.
 *
 * Respects the system "Remove animations" accessibility setting out of the
 * box (Section 34): when active, idle wave motion and the master shimmer
 * are disabled, while the functional fill-level transition is preserved
 * (just switched from a spring to a short, linear tween).
 *
 * Phase 6 adds an app-level override on top of the system reading:
 * [LocalReducedMotionPreference] (set from the Settings screen's Reduce
 * Motion toggle) is OR-combined into the [reduceMotion] default, so turning
 * the app setting on forces calm animation even when the system setting is
 * off — but leaving it off never fights a system setting that's already on.
 */
@Composable
fun JellyProgressCanvas(
    visualProgress: Float,
    stageColor: Color,
    modifier: Modifier = Modifier,
    isMasterStage: Boolean = false,
    cardCornerRadius: Dp = 0.dp,
    reduceMotion: Boolean = rememberSystemReduceMotionEnabled().value || LocalReducedMotionPreference.current
) {
    val clampedProgress = visualProgress.coerceIn(0f, 1f)
    val effectiveProgress = if (isMasterStage) 1f else clampedProgress

    val animatedProgress by animateFloatAsState(
        targetValue = effectiveProgress,
        animationSpec = if (reduceMotion) {
            tween(durationMillis = 260, easing = LinearEasing)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "jellyFillProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "jellyIdleMotion")

    val wavePhaseState: State<Float> = if (reduceMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavePhase"
        )
    }
    val wavePhase by wavePhaseState

    val waveAmplitudeFactorState: State<Float> = if (reduceMotion) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveAmplitude"
        )
    }
    val waveAmplitudeFactor by waveAmplitudeFactorState

    val shimmerOffsetState: State<Float> = if (reduceMotion || !isMasterStage) {
        remember { mutableStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -0.4f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "masterShimmer"
        )
    }
    val shimmerOffset by shimmerOffsetState

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return@Canvas

        val baseAmplitudePx = if (reduceMotion) {
            1.5.dp.toPx()
        } else {
            7.dp.toPx() * waveAmplitudeFactor
        }
        val amplitudePx = if (isMasterStage) baseAmplitudePx * 0.3f else baseAmplitudePx
        val fillEdgeX = width * animatedProgress

        val primaryPath = buildJellyBoundaryPath(
            width = width,
            height = height,
            fillEdgeX = fillEdgeX,
            amplitude = amplitudePx,
            phase = wavePhase
        )
        drawPath(path = primaryPath, color = stageColor.copy(alpha = 0.42f))

        // Secondary, slightly offset and quieter layer for parallax depth —
        // two waves out of phase read as "liquid" far more than one flat fill.
        val secondaryFillEdgeX = (fillEdgeX - 6.dp.toPx()).coerceAtLeast(0f)
        val secondaryPath = buildJellyBoundaryPath(
            width = width,
            height = height,
            fillEdgeX = secondaryFillEdgeX,
            amplitude = amplitudePx * 0.65f,
            phase = wavePhase + (PI / 2f).toFloat()
        )
        drawPath(path = secondaryPath, color = stageColor.copy(alpha = 0.22f))

        if (isMasterStage) {
            if (!reduceMotion) {
                val sweepWidth = width * 0.35f
                val sweepCenterX = width * shimmerOffset
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        start = Offset(sweepCenterX - sweepWidth / 2f, 0f),
                        end = Offset(sweepCenterX + sweepWidth / 2f, height)
                    ),
                    topLeft = Offset.Zero,
                    size = Size(width, height)
                )
            }

            if (cardCornerRadius > 0.dp) {
                val strokeWidthPx = 1.5.dp.toPx()
                val radiusPx = cardCornerRadius.toPx()
                drawRoundRect(
                    color = stageColor.copy(alpha = 0.55f),
                    topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                    size = Size(width - strokeWidthPx, height - strokeWidthPx),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }
    }
}

/**
 * Builds the fill region as a path from the left edge of the canvas to an
 * organic, vertically-sinusoidal boundary near [fillEdgeX] — this is what
 * makes the fill read as a liquid coastline rather than a hard progress bar.
 * The boundary is sampled at [segments] points down the height and joined
 * with cubic Bezier segments for a smooth, rounded squiggle.
 */
private fun buildJellyBoundaryPath(
    width: Float,
    height: Float,
    fillEdgeX: Float,
    amplitude: Float,
    phase: Float,
    segments: Int = 8
): Path {
    val path = Path()

    fun boundaryX(t: Float): Float {
        val angle = phase + t * 4f * PI.toFloat()
        return (fillEdgeX + amplitude * sin(angle)).coerceIn(0f, width)
    }

    val topX = boundaryX(0f)
    path.moveTo(0f, 0f)
    path.lineTo(topX, 0f)

    var prevX = topX
    var prevY = 0f
    for (i in 1..segments) {
        val t = i.toFloat() / segments
        val y = t * height
        val x = boundaryX(t)
        val midY = (prevY + y) / 2f
        path.cubicTo(prevX, midY, x, midY, x, y)
        prevX = x
        prevY = y
    }

    path.lineTo(0f, height)
    path.close()
    return path
}

/**
 * Tracks the system-wide "Remove animations" accessibility setting
 * (`Settings.Global.ANIMATOR_DURATION_SCALE == 0`), live, via a
 * [ContentObserver]. This is the honest, zero-configuration default for
 * "reduce motion" (Section 34); an in-app Settings toggle added in a later
 * phase can simply override the [JellyProgressCanvas.reduceMotion] parameter
 * without any change to this component.
 */
@Composable
private fun rememberSystemReduceMotionEnabled(): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(readAnimatorDurationScale(context) == 0f) }

    DisposableEffect(context) {
        val resolver = context.contentResolver
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                state.value = readAnimatorDurationScale(context) == 0f
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            /* notifyForDescendants = */ false,
            observer
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return state
}

private fun readAnimatorDurationScale(context: android.content.Context): Float =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
