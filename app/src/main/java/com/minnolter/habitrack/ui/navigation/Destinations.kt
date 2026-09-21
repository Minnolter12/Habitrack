package com.minnolter.habitrack.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose destinations.
 */
sealed interface Destination {

    @Serializable
    data object Onboarding : Destination

    @Serializable
    data object Home : Destination

    @Serializable
    data class Detail(val habitId: Long) : Destination

    @Serializable
    data object Settings : Destination

    @Serializable
    data class AddEditHabit(val habitId: Long? = null) : Destination

    @Serializable
    data class CreateHabit(val isFirstRunOnboarding: Boolean = false) : Destination
}
