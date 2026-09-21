package com.minnolter.habitrack.ui.screens.create.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun IntroStep(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = "Welcome to Habitrack",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Time shapes you.",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE040FB),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Mastery is not an accident—it's 10,000 hours of deliberate focus.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.90f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Whether you are starting from zero or carrying years of prior practice, Habitrack accurately calculates your baseline foundation and visualizes your evolution through organic liquid stages.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
