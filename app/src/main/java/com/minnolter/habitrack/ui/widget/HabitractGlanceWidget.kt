package com.minnolter.habitrack.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.minnolter.habitrack.HabitractApplication
import com.minnolter.habitrack.MainActivity
import com.minnolter.habitrack.domain.model.ProgressionStage
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.util.formatAccumulatedDuration
import kotlinx.coroutines.flow.firstOrNull

class HabitractGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as? HabitractApplication
        val repository = app?.repository

        val habits = repository?.observeHabits()?.firstOrNull() ?: emptyList()
        val topHabit = habits.firstOrNull()

        val lifetimeMinutes = if (topHabit != null && repository != null) {
            repository.observeMinutes(topHabit.id, TimeRange.LIFETIME).firstOrNull() ?: 0
        } else {
            0
        }

        val weekMinutes = if (topHabit != null && repository != null) {
            repository.observeMinutes(topHabit.id, TimeRange.WEEK).firstOrNull() ?: 0
        } else {
            0
        }

        val stage = ProgressionStage.fromMinutes(lifetimeMinutes.toLong())

        provideContent {
            GlanceTheme {
                WidgetContent(
                    habitName = topHabit?.name ?: "Habitrack",
                    stageName = stage.displayName,
                    colorHex = topHabit?.colorHex ?: "#00E5FF",
                    periodDurationStr = formatAccumulatedDuration(weekMinutes.toLong()) + " this week",
                    lifetimeDurationStr = formatAccumulatedDuration(lifetimeMinutes.toLong()) + " total"
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(
        habitName: String,
        stageName: String,
        colorHex: String,
        periodDurationStr: String,
        lifetimeDurationStr: String
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.parseColor("#1B1822")))
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🦉 Habitrack",
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.parseColor("#00E5FF")),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = stageName,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.parseColor("#E040FB")),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = habitName,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.parseColor("#FFFFFF")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = periodDurationStr,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.parseColor("#00E5FF")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = lifetimeDurationStr,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.parseColor("#B3FFFFFF")),
                    fontSize = 12.sp
                )
            )
        }
    }
}

class HabitractGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitractGlanceWidget()
}
