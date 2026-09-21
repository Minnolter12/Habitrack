package com.minnolter.habitrack.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose destinations (requires the
 * `kotlinx.serialization` plugin — see `app/build.gradle.kts`). Each
 * destination carries its own arguments as real Kotlin properties instead of
 * a hand-built string route, so a typo or a missing argument is a compile
 * error rather than a runtime crash on `navigate("habit_detail/$id")`.
 */
sealed interface Destination {

    @Serializable
    data object Home : Destination

    @Serializable
    data class Detail(val habitId: Long) : Destination

    @Serializable
    data object Settings : Destination

    /** [habitId] null means "creating a new habit"; non-null means "editing this one." */
    @Serializable
    data class AddEditHabit(val habitId: Long? = null) : Destination
}
