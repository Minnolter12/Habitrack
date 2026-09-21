package com.minnolter.habitrack.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.ui.screens.addhabit.AddEditHabitRoute
import com.minnolter.habitrack.ui.screens.addhabit.AddEditHabitViewModel
import com.minnolter.habitrack.ui.screens.addhabit.AddEditHabitViewModelFactory
import com.minnolter.habitrack.ui.screens.detail.HabitDetailRoute
import com.minnolter.habitrack.ui.screens.detail.HabitDetailViewModel
import com.minnolter.habitrack.ui.screens.detail.HabitDetailViewModelFactory
import com.minnolter.habitrack.ui.screens.home.HomeRoute
import com.minnolter.habitrack.ui.screens.home.HomeViewModel
import com.minnolter.habitrack.ui.screens.home.HomeViewModelFactory
import com.minnolter.habitrack.ui.screens.settings.SettingsRoute
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModel
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModelFactory

/**
 * Motion duration for cross-destination transitions. 300ms sits at the
 * "emphasized" end of Material 3's duration scale for a full-screen
 * transition, without lingering long enough to feel sluggish on the
 * frequent Home ↔ Detail hop this app is built around.
 */
private const val NAV_TRANSITION_MS = 300

/**
 * A new destination slides in from the trailing edge while fading in, and the
 * one it covers slides slightly out to the leading edge while fading out —
 * the standard M3 "forward navigation" pairing. Popping the back stack runs
 * the same pairing in reverse, which is what makes Back feel like undoing
 * Forward rather than a different transition altogether.
 */
private fun AnimatedContentTransitionScope<*>.forwardEnter(): EnterTransition =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NAV_TRANSITION_MS)
    ) + fadeIn(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.forwardExit(): ExitTransition =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        animationSpec = tween(NAV_TRANSITION_MS)
    ) + fadeOut(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.backwardEnter(): EnterTransition =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NAV_TRANSITION_MS)
    ) + fadeIn(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.backwardExit(): ExitTransition =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        animationSpec = tween(NAV_TRANSITION_MS)
    ) + fadeOut(animationSpec = tween(NAV_TRANSITION_MS))

/**
 * The full navigation shell (Sections 4, 24, 30, 31, 40): [Destination.Home]
 * as the launch root, with [Destination.Detail], [Destination.Settings], and
 * [Destination.AddEditHabit] reachable from it. All four destinations share
 * one [HabitractRepository] instance (and, for Settings, one
 * [SettingsDataStore] / [DatabaseBackupManager] pair) passed down from
 * [com.habitract.app.HabitractApplication] — there's still no DI framework,
 * so this is the single place those singletons fan out to their ViewModels.
 *
 * Reorder mode (Section 24) is deliberately Home-local state, not a
 * destination: [HomeViewModel] already owns `isReorderMode`, so Settings'
 * "Reorder habits" row simply pops back to Home and calls
 * [HomeViewModel.enterReorderMode] on the same, already-alive instance,
 * rather than navigating to a new screen that would need to duplicate that
 * state.
 */
@Composable
fun HabitractNavHost(
    repository: HabitractRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: DatabaseBackupManager,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Home,
        enterTransition = { forwardEnter() },
        exitTransition = { forwardExit() },
        popEnterTransition = { backwardEnter() },
        popExitTransition = { backwardExit() }
    ) {
        composable<Destination.Home> { backStackEntry ->
            // Scoped to the Home route itself (not the NavGraph) so reorder
            // mode and the dashboard filter reset if the user ever leaves
            // Home and returns via a fresh back-stack entry — but survive the
            // round trip to Detail/Settings/AddEditHabit and back, since
            // those all push on top of this same entry rather than replacing it.
            val homeViewModel: HomeViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = HomeViewModelFactory(repository)
            )

            HomeRoute(
                viewModel = homeViewModel,
                onAddHabitClick = {
                    navController.navigate(Destination.AddEditHabit())
                },
                onSettingsClick = {
                    navController.navigate(Destination.Settings)
                },
                onHabitLongPress = { habitId ->
                    navController.navigate(Destination.Detail(habitId))
                }
            )
        }

        composable<Destination.Detail> { backStackEntry ->
            val destination: Destination.Detail = backStackEntry.toRoute()

            val detailViewModel: HabitDetailViewModel = viewModel(
                key = "habit_detail_${destination.habitId}",
                factory = HabitDetailViewModelFactory(destination.habitId, repository)
            )

            HabitDetailRoute(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Destination.Settings> {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(settingsDataStore, backupManager)
            )

            SettingsRoute(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onReorderHabitsClick = {
                    // The Home ViewModel instance is still alive on the back
                    // stack (see the comment above); fetching it by its
                    // NavBackStackEntry's own ViewModelStore (not the
                    // `viewModel()` composable helper, which can't be called
                    // from a plain click lambda) returns that exact same
                    // instance rather than creating a new one, so flipping
                    // its isReorderMode here is what lets both the Settings
                    // entry point (Section 24 spec text) and the Home top-bar
                    // action (this phase's prompt) drive the same reorder
                    // session.
                    navController.previousBackStackEntry?.let { homeEntry ->
                        ViewModelProvider(homeEntry, HomeViewModelFactory(repository))
                            .get(HomeViewModel::class.java)
                            .enterReorderMode()
                    }
                    navController.popBackStack()
                }
            )
        }

        composable<Destination.AddEditHabit> { backStackEntry ->
            val destination: Destination.AddEditHabit = backStackEntry.toRoute()

            val addEditViewModel: AddEditHabitViewModel = viewModel(
                key = "add_edit_habit_${destination.habitId ?: "new"}",
                factory = AddEditHabitViewModelFactory(destination.habitId, repository)
            )

            AddEditHabitRoute(
                viewModel = addEditViewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
