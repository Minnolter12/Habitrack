package com.minnolter.habitrack.ui.screens.create.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.minnolter.habitrack.domain.model.HabitCategory
import com.minnolter.habitrack.domain.model.PresetActivity

@Composable
fun StepDiscipline(
    habitTitle: String,
    selectedCategory: HabitCategory,
    searchQuery: String,
    filteredPresets: List<PresetActivity>,
    onTitleChanged: (String) -> Unit,
    onCategorySelected: (HabitCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPresetSelected: (PresetActivity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Step 1: Discipline & Identity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "What skill or habit are you committing to?",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Custom Title Input
        OutlinedTextField(
            value = habitTitle,
            onValueChange = onTitleChanged,
            placeholder = { Text("e.g., Violin, Coding, Distance Running") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7C4DFF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.20f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x1AFFFFFF),
                unfocusedContainerColor = Color(0x0EFFFFFF)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.displayName) },
                    shape = RoundedCornerShape(percent = 50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0x447C4DFF),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x14FFFFFF),
                        labelColor = Color.White.copy(alpha = 0.65f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar for 500+ presets
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search 500+ activity presets...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(0.6f)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x14FFFFFF),
                unfocusedContainerColor = Color(0x0AFFFFFF)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Searchable Presets List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPresets) { preset ->
                PresetActivityRow(
                    preset = preset,
                    isSelected = preset.name.equals(habitTitle, ignoreCase = true),
                    onClick = { onPresetSelected(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetActivityRow(
    preset: PresetActivity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0x337C4DFF) else Color(0x12FFFFFF))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = preset.imageUrl,
                contentDescription = null,
                alpha = 0.45f,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = preset.category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}
