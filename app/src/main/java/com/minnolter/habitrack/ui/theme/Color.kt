package com.minnolter.habitrack.ui.theme

import androidx.compose.ui.graphics.Color

// Static fallback palette for devices below Android 12 (no dynamic color) and
// for anyone who wants Habitract to look the same everywhere. Seeded from the
// same violet family as ProgressionStage's early-stage colors so the app
// still feels recognizably "Habitract" even without dynamic color
// (Section 37: "The visual identity should remain recognizably Habitract
// even when dynamic colors are enabled").

val SeedPrimaryLight = Color(0xFF6650A4)
val SeedOnPrimaryLight = Color(0xFFFFFFFF)
val SeedPrimaryContainerLight = Color(0xFFEADDFF)
val SeedOnPrimaryContainerLight = Color(0xFF21005D)
val SeedSecondaryLight = Color(0xFF625B71)
val SeedBackgroundLight = Color(0xFFFFFBFE)
val SeedSurfaceLight = Color(0xFFFFFBFE)

val SeedPrimaryDark = Color(0xFFD0BCFF)
val SeedOnPrimaryDark = Color(0xFF381E72)
val SeedPrimaryContainerDark = Color(0xFF4F378B)
val SeedOnPrimaryContainerDark = Color(0xFFEADDFF)
val SeedSecondaryDark = Color(0xFFCCC2DC)
val SeedBackgroundDark = Color(0xFF1C1B1F)
val SeedSurfaceDark = Color(0xFF1C1B1F)
