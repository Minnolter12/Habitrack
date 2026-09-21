package com.minnolter.habitrack.ui.screens.home.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.minnolter.habitrack.domain.model.TimeRange
import com.minnolter.habitrack.util.formatAccumulatedDuration

/**
 * The global dashboard (Sections 9–10): the aggregate practice time across
 * every habit for whichever [selectedRange] is active, plus the filter
 * chips used to switch ranges. [totalMinutes] must already be pre-aggregated
 * for [selectedRange] by the caller (see `HomeViewModel`) — this composable
 * only ever renders numbers, it never queries or sums anything itself.
 *
 * Lifetime is the required default range (Section 10) precisely so a fresh
 * install opens on the user's total accumulated investment rather than a
 * discouraging "0 hours today."
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatAccumulatedDuration(totalMinutes),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
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
                .padding(top = 20.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.entries.forEach { range ->
                FilterChip(
                    selected = range == selectedRange,
                    onClick = { onRangeSelected(range) },
                    label = { Text(range.displayLabel()) }
                )
            }
        }
    }
}

private fun habitCountLabel(habitCount: Int): String = when (habitCount) {
    0 -> "No habits yet"
    1 -> "Across 1 habit"
    else -> "Across $habitCount habits"
}

/** UI-facing labels for [TimeRange] — kept out of the domain model on purpose. */
private fun TimeRange.displayLabel(): String = when (this) {
    TimeRange.LIFETIME -> "Lifetime"
    TimeRange.TODAY -> "Today"
    TimeRange.WEEK -> "This Week"
    TimeRange.MONTH -> "This Month"
    TimeRange.YEAR -> "This Year"
}
