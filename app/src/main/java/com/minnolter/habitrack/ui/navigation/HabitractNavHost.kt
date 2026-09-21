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
import com.minnolter.habitrack.ui.screens.create.CreateHabitViewModel
import com.minnolter.habitrack.ui.screens.create.CreateHabitViewModelFactory
import com.minnolter.habitrack.ui.screens.create.CreateHabitWizardScreen
import com.minnolter.habitrack.ui.screens.detail.HabitDetailRoute
import com.minnolter.habitrack.ui.screens.detail.HabitDetailViewModel
import com.minnolter.habitrack.ui.screens.detail.HabitDetailViewModelFactory
import com.minnolter.habitrack.ui.screens.home.HomeRoute
import com.minnolter.habitrack.ui.screens.home.HomeViewModel
import com.minnolter.habitrack.ui.screens.home.HomeViewModelFactory
import com.minnolter.habitrack.ui.screens.settings.SettingsRoute
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModel
import com.minnolter.habitrack.ui.screens.settings.SettingsViewModelFactory

private const val NAV_TRANSITION_MS = 300

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
            val homeViewModel: HomeViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = HomeViewModelFactory(repository)
            )

            HomeRoute(
                viewModel = homeViewModel,
                onAddHabitClick = {
                    navController.navigate(Destination.CreateHabit(isFirstRunOnboarding = false))
                },
                onSettingsClick = {
                    navController.navigate(Destination.Settings)
                },
                onHabitLongPress = { habitId ->
                    navController.navigate(Destination.Detail(habitId))
                }
            )
        }

        composable<Destination.CreateHabit> { backStackEntry ->
            val destination: Destination.CreateHabit = backStackEntry.toRoute()

            val createViewModel: CreateHabitViewModel = viewModel(
                factory = CreateHabitViewModelFactory(
                    isFirstRunOnboarding = destination.isFirstRunOnboarding,
                    repository = repository,
                    settingsDataStore = settingsDataStore
                )
            )

            CreateHabitWizardScreen(
                viewModel = createViewModel,
                onDismiss = { navController.popBackStack() },
                onFinished = { habitId ->
                    navController.popBackStack()
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
