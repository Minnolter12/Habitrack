package com.minnolter.habitrack.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.util.formatAccumulatedDuration

private val HOME_DASHBOARD_TIME_RANGES = listOf(
    TimeRange.LIFETIME,
    TimeRange.TODAY,
    TimeRange.WEEK,
    TimeRange.MONTH,
    TimeRange.YEAR
)

/**
 * The global dashboard header: displays the total hours invested, habit count subtitle,
 * and edge-to-edge glassmorphic range filter chips (Today, Week, Month, Year, Lifetime).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSummaryHeader(
    selectedRange: TimeRange,
    totalMinutes: Long,
    habitCount: Int,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val formattedTime = formatAccumulatedDuration(totalMinutes)
        
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = habitCountLabel(habitCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "", modifier = Modifier.padding(start = 10.dp))

            HOME_DASHBOARD_TIME_RANGES.forEach { range ->
                val isSelected = range == selectedRange
                FilterChip(
                    selected = isSelected,
                    onClick = { onRangeSelected(range) },
                    label = { 
                        Text(
                            text = range.displayLabel(),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ) 
                    },
                    shape = RoundedCornerShape(percent = 50),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF80DEEA), Color(0xFFE040FB), Color(0xFFFFD54F))
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.08f))
                            )
                        }
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x337C4DFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x14FFFFFF),
                        labelColor = Color.White.copy(alpha = 0.70f)
                    )
                )
            }

            Text(text = "", modifier = Modifier.padding(end = 10.dp))
        }
    }
}

private fun habitCountLabel(habitCount: Int): String = when (habitCount) {
    0 -> "No habits yet"
    1 -> "Across 1 habit"
    else -> "Across $habitCount habits"
}

/** UI-facing labels for Home dashboard [TimeRange] */
private fun TimeRange.displayLabel(): String = when (this) {
    TimeRange.TODAY -> "Today"
    TimeRange.WEEK -> "Week"
    TimeRange.MONTH -> "Month"
    TimeRange.YEAR -> "Year"
    TimeRange.LIFETIME -> "Lifetime"
}
