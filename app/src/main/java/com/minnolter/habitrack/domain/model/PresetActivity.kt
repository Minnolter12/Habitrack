package com.minnolter.habitrack.domain.model

enum class HabitCategory(val displayName: String) {
    CRAFT_AND_ART("Craft & Art"),
    MUSIC("Music"),
    SOFTWARE_ENGINEERING("Software & Engineering"),
    ATHLETICS_PHYSICAL("Athletics & Physical"),
    ACADEMIC_LANGUAGE("Academic & Language"),
    TRADES_CULINARY("Trades & Culinary"),
    GAMES_MIND("Games & Mind"),
    WELLNESS_LIFE("Wellness & Life"),
    OTHER("Other")
}

data class PresetActivity(
    val name: String,
    val category: HabitCategory,
    val defaultColorHex: String = "#7C4DFF",
    val imageUrl: String
)
