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
import androidx.glance.appwidget.SizeMode
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

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = runCatching {
            val app = context.applicationContext as? HabitractApplication
            val repository = app?.repository

            val habits = repository?.observeHabits()?.firstOrNull() ?: emptyList()
            if (habits.isEmpty()) {
                WidgetData(
                    habitName = "Habitrack",
                    stageName = "Start Journey",
                    colorHex = "#00E5FF",
                    periodDurationStr = "0h this week",
                    lifetimeDurationStr = "0h total"
                )
            } else {
                var bestHabit = habits.first()
                var maxWeekMinutes = 0

                habits.forEach { habit ->
                    val mins = repository?.observeMinutes(habit.id, TimeRange.WEEK)?.firstOrNull() ?: 0
                    if (mins >= maxWeekMinutes) {
                        maxWeekMinutes = mins
                        bestHabit = habit
                    }
                }

                val lifetimeMins = repository?.observeMinutes(bestHabit.id, TimeRange.LIFETIME)?.firstOrNull() ?: 0
                val stage = ProgressionStage.fromMinutes(lifetimeMins.toLong())

                WidgetData(
                    habitName = bestHabit.name,
                    stageName = stage.displayName,
                    colorHex = bestHabit.colorHex,
                    periodDurationStr = "${formatAccumulatedDuration(maxWeekMinutes.toLong())} this week",
                    lifetimeDurationStr = "${formatAccumulatedDuration(lifetimeMins.toLong())} total"
                )
            }
        }.getOrDefault(
            WidgetData(
                habitName = "Habitrack",
                stageName = "Mastery Tracker",
                colorHex = "#00E5FF",
                periodDurationStr = "Tap to open",
                lifetimeDurationStr = "Time shapes you."
            )
        )

        provideContent {
            GlanceTheme {
                WidgetContent(widgetData)
            }
        }
    }

    @Composable
    private fun WidgetContent(data: WidgetData) {
        val bgParsedColor = parseColorSafely("#1B1822", android.graphics.Color.DKGRAY)
        val accentParsedColor = parseColorSafely(data.colorHex, android.graphics.Color.CYAN)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(bgParsedColor))
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
                        color = ColorProvider(parseColorSafely("#00E5FF", android.graphics.Color.CYAN)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = data.stageName,
                    style = TextStyle(
                        color = ColorProvider(accentParsedColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Text(
                text = data.habitName,
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.WHITE),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = data.periodDurationStr,
                style = TextStyle(
                    color = ColorProvider(parseColorSafely("#00E5FF", android.graphics.Color.CYAN)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            Text(
                text = data.lifetimeDurationStr,
                style = TextStyle(
                    color = ColorProvider(parseColorSafely("#B3FFFFFF", android.graphics.Color.LTGRAY)),
                    fontSize = 12.sp
                )
            )
        }
    }

    private fun parseColorSafely(colorHex: String, fallback: Int): Int {
        return runCatching { android.graphics.Color.parseColor(colorHex) }.getOrDefault(fallback)
    }

    private data class WidgetData(
        val habitName: String,
        val stageName: String,
        val colorHex: String,
        val periodDurationStr: String,
        val lifetimeDurationStr: String
    )
}

class HabitractGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitractGlanceWidget()
}
