package com.minnolter.habitrack.ui.screens.create.steps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.minnolter.habitrack.domain.model.PresetActivity

@Composable
fun StepDiscipline(
    habitTitle: String,
    searchQuery: String,
    filteredPresets: List<PresetActivity>,
    isCustomHabit: Boolean,
    onTitleChanged: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onPresetSelected: (PresetActivity) -> Unit,
    onEnableCustomHabit: () -> Unit,
    onCustomImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onCustomImageSelected(uri.toString())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Step 1: Choose Your Discipline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = if (isCustomHabit) "Create a custom habit & pick a photo/icon below." else "Select from 100 unique activity presets or make a custom habit.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        if (isCustomHabit) {
            OutlinedTextField(
                value = habitTitle,
                onValueChange = onTitleChanged,
                placeholder = { Text("Enter custom habit name...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Choose an Icon / Photo from Preset Activities:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.80f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Larger icon choices row so photos are clearly visible
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredPresets) { preset ->
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .clickable { onPresetSelected(preset) }
                    ) {
                        AsyncImage(
                            model = preset.imageUrl,
                            contentDescription = null,
                            alpha = 0.70f,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import Custom Photo from Gallery")
            }
        } else {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search 100 unique activities...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White.copy(0.6f)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets List expanding to occupy remaining available height down to bottom
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEnableCustomHabit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x2200E5FF),
                    contentColor = Color(0xFF00E5FF)
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Make Custom Habit", fontWeight = FontWeight.SemiBold)
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
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = preset.imageUrl,
                contentDescription = null,
                alpha = 0.55f,
                modifier = Modifier.fillMaxSize(),
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

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E5FF),
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0x14FFFFFF),
    unfocusedContainerColor = Color(0x0AFFFFFF)
)
