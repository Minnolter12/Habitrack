package com.minnolter.habitrack.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.minnolter.habitrack.data.local.DatabaseBackupManager
import com.minnolter.habitrack.data.local.datastore.SettingsDataStore
import com.minnolter.habitrack.domain.repository.HabitractRepository
import com.minnolter.habitrack.ui.screens.about.AboutScreen
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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin

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

private fun AnimatedContentTransitionScope<*>.homeExitToDetail(): ExitTransition =
    scaleOut(
        targetScale = 0.88f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        )
    ) + fadeOut(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.homePopEnterFromDetail(): EnterTransition =
    scaleIn(
        initialScale = 0.88f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    ) + fadeIn(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.detailEnterFromHome(): EnterTransition =
    scaleIn(
        initialScale = 0.72f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    ) + fadeIn(animationSpec = tween(NAV_TRANSITION_MS))

private fun AnimatedContentTransitionScope<*>.detailPopExitToHome(): ExitTransition =
    scaleOut(
        targetScale = 0.72f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        )
    ) + fadeOut(animationSpec = tween(NAV_TRANSITION_MS))

@Composable
fun HabitractNavHost(
    repository: HabitractRepository,
    settingsDataStore: SettingsDataStore,
    backupManager: DatabaseBackupManager,
    authManager: com.minnolter.habitrack.data.auth.FirebaseAuthManager,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val isHome = currentDestination?.contains("Home") == true
    val isSettings = currentDestination?.contains("Settings") == true
    val showBottomBar = isHome || isSettings
    val activeRoute = if (isHome) "home" else if (isSettings) "settings" else null

    val navItems = listOf(
        NavItem(
            route = "home",
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavItem(
            route = "settings",
            label = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destination.Home,
            enterTransition = { forwardEnter() },
            exitTransition = { forwardExit() },
            popEnterTransition = { backwardEnter() },
            popExitTransition = { backwardExit() }
        ) {
            composable<Destination.Home>(
                exitTransition = {
                    if (targetState.destination.route?.contains("Detail") == true) {
                        homeExitToDetail()
                    } else {
                        forwardExit()
                    }
                },
                popEnterTransition = {
                    if (initialState.destination.route?.contains("Detail") == true) {
                        homePopEnterFromDetail()
                    } else {
                        backwardEnter()
                    }
                }
            ) { backStackEntry ->
                val homeViewModel: HomeViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = HomeViewModelFactory(repository, settingsDataStore)
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

            composable<Destination.Detail>(
                enterTransition = { detailEnterFromHome() },
                exitTransition = { detailPopExitToHome() },
                popEnterTransition = { detailEnterFromHome() },
                popExitTransition = { detailPopExitToHome() }
            ) { backStackEntry ->
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
                    factory = SettingsViewModelFactory(repository, settingsDataStore, backupManager, authManager)
                )

                SettingsRoute(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onAboutClick = {
                        navController.navigate(Destination.About)
                    }
                )
            }

            composable<Destination.About> {
                AboutScreen(
                    onBack = { navController.popBackStack() }
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

        if (showBottomBar) {
            TranslucentPillNavigationBar(
                items = navItems,
                currentRoute = activeRoute,
                onItemSelected = { item ->
                    if (item.route == activeRoute) return@TranslucentPillNavigationBar

                    if (item.route == "home") {
                        navController.navigate(Destination.Home) {
                            popUpTo(Destination.Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (item.route == "settings") {
                        navController.navigate(Destination.Settings) {
                            popUpTo(Destination.Home)
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
