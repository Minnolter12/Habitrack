package com.minnolter.habitrack.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.minnolter.habitrack.domain.model.Habit
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.util.formatAccumulatedDuration

/**
 * The primary Home-screen unit (Sections 12–14): a rounded, elevated card
 * with the habit name top-right, accumulated duration centered, and current
 * [ProgressionStage] tag bottom-right, all layered over the [JellyProgressCanvas]
 * fill and an optional, heavily subdued background image.
 *
 * Per Section 39: a single tap opens quick logging ([onTap]); a long press
 * opens the detailed statistics screen ([onLongPress]). Neither gesture is
 * exposed as a drag target here — reordering only happens in the dedicated
 * Reorder mode (Section 24), outside this composable's concern.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    lifetimeMinutes: Long,
    stage: ProgressionStage,
    visualProgress: Float,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 168.dp
) {
    val shape = RoundedCornerShape(28.dp)
    val isMaster = stage == ProgressionStage.MASTER
    val stageColor = stage.toColor()
    val hasBackgroundImage = !habit.imageUri.isNullOrBlank()
    val formattedDuration = formatAccumulatedDuration(lifetimeMinutes)

    val contentColor = if (hasBackgroundImage) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = contentColor.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = if (isMaster) 10.dp else 3.dp,
                shape = shape,
                ambientColor = stageColor.copy(alpha = 0.35f),
                spotColor = stageColor.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress,
                onClickLabel = "Log practice time",
                onLongClickLabel = "View ${habit.name} statistics"
            )
            .semantics {
                role = Role.Button
                contentDescription =
                    "${habit.name}. $formattedDuration invested. ${stage.displayName} stage."
            }
    ) {
        if (hasBackgroundImage) {
            AsyncImage(
                model = habit.imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Heavy subdual so the jelly fill and text stay the focal point
            // and remain readable (Section 13) — the image is atmosphere,
            // not content.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.50f),
                                Color.Black.copy(alpha = 0.62f)
                            )
                        )
                    )
            )
        }

        JellyProgressCanvas(
            visualProgress = visualProgress,
            stageColor = stageColor,
            isMasterStage = isMaster,
            cardCornerRadius = 28.dp,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = habit.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = secondaryContentColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp)
        )

        Text(
            text = formattedDuration,
            style = if (isMaster) {
                MaterialTheme.typography.displaySmall
            } else {
                MaterialTheme.typography.headlineMedium
            },
            fontWeight = FontWeight.SemiBold,
            color = if (isMaster) stageColor else contentColor,
            modifier = Modifier.align(Alignment.Center)
        )

        StageTag(
            stage = stage,
            isMaster = isMaster,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 14.dp, end = 20.dp)
        )
    }
}

@Composable
private fun StageTag(
    stage: ProgressionStage,
    isMaster: Boolean,
    modifier: Modifier = Modifier
) {
    val stageColor = stage.toColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = stageColor.copy(alpha = if (isMaster) 0.30f else 0.20f),
        contentColor = stageColor
    ) {
        Text(
            text = stage.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isMaster) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

/** Maps a [ProgressionStage]'s stored hex token to a Compose [Color]. */
private fun ProgressionStage.toColor(): Color =
    Color(android.graphics.Color.parseColor(colorHex))
