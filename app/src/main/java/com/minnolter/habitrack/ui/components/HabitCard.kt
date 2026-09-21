package com.minnolter.habitrack.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.minnolter.habitrack.util.getDynamicHabitColor

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

    // 100+ dynamic color hues that shift every 10 logged hours
    val dynamicStageColor = getDynamicHabitColor(
        baseStage = stage,
        lifetimeMinutes = lifetimeMinutes,
        customColorHex = habit.colorHex
    )

    val hasBackgroundImage = !habit.imageUri.isNullOrBlank()
    val formattedDuration = formatAccumulatedDuration(lifetimeMinutes)

    val contentColor = Color.White
    val secondaryContentColor = contentColor.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shadow(
                elevation = if (isMaster) 10.dp else 4.dp,
                shape = shape,
                ambientColor = dynamicStageColor.copy(alpha = 0.35f),
                spotColor = dynamicStageColor.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(Color(0xFF1B1822).copy(alpha = 0.75f))
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
        // Left-aligned, highly faded, subtle background image
        if (hasBackgroundImage) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {
                AsyncImage(
                    model = habit.imageUri,
                    contentDescription = null,
                    alpha = 0.18f,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.55f)
                        .align(Alignment.CenterStart),
                    contentScale = ContentScale.Crop
                )
                // Soft gradient fading out to the right
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF120F1A).copy(alpha = 0.70f),
                                    Color(0xFF120F1A)
                                )
                            )
                        )
                )
            }
        }

        JellyProgressCanvas(
            visualProgress = visualProgress,
            stageColor = dynamicStageColor,
            isMasterStage = isMaster,
            cardCornerRadius = 28.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Glassmorphic holographic shell over jelly, under text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .holographicSurface(stage = stage)
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
            color = if (isMaster) dynamicStageColor else contentColor,
            modifier = Modifier.align(Alignment.Center)
        )

        StageTag(
            stage = stage,
            stageColor = dynamicStageColor,
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
    stageColor: Color,
    isMaster: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = stageColor.copy(alpha = if (isMaster) 0.35f else 0.22f),
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
