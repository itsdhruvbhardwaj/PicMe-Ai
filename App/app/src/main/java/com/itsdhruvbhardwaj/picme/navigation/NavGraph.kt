package com.itsdhruvbhardwaj.picme.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.itsdhruvbhardwaj.picme.BuildConfig
import com.itsdhruvbhardwaj.picme.data.repository.StyleRepository
import com.itsdhruvbhardwaj.picme.ui.auth.AuthState
import com.itsdhruvbhardwaj.picme.ui.auth.AuthViewModel
import com.itsdhruvbhardwaj.picme.ui.components.PicMeBottomNavigationBar
import com.itsdhruvbhardwaj.picme.ui.screens.auth.LoginScreen
import com.itsdhruvbhardwaj.picme.ui.screens.auth.SignupScreen
import com.itsdhruvbhardwaj.picme.ui.screens.create.GenerationScreen
import com.itsdhruvbhardwaj.picme.ui.screens.create.ResultScreen
import com.itsdhruvbhardwaj.picme.ui.screens.create.SelectPhotosScreen
import com.itsdhruvbhardwaj.picme.ui.screens.home.HomeScreen
import com.itsdhruvbhardwaj.picme.ui.screens.home.HomeViewModel
import com.itsdhruvbhardwaj.picme.ui.screens.home.StyleDetailsScreen
import com.itsdhruvbhardwaj.picme.ui.screens.home.StyleDetailsViewModel
import com.itsdhruvbhardwaj.picme.ui.screens.home.StylesGridScreen
import com.itsdhruvbhardwaj.picme.ui.screens.home.StylesGridViewModel
import com.itsdhruvbhardwaj.picme.ui.screens.onboarding.OnboardingScreen
import com.itsdhruvbhardwaj.picme.ui.screens.profile.ProfileScreen
import com.itsdhruvbhardwaj.picme.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    styleRepository: StyleRepository
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("picme_prefs", Context.MODE_PRIVATE)
    
    val authState by authViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Create.route,
        Screen.History.route,
        Screen.Profile.route
    )

    val startDestination = if (BuildConfig.DEBUG) Screen.Home.route else Screen.Splash.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                PicMeBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(onSplashFinished = {
                        val isOnboardingCompleted = prefs.getBoolean("onboarding_completed", false)
                        
                        val destination = when {
                            authState is AuthState.Authenticated -> Screen.Home.route
                            !isOnboardingCompleted -> Screen.Onboarding.route
                            else -> Screen.Login.route
                        }
                        
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    })
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinish = {
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        onSkip = {
                            prefs.edit().putBoolean("onboarding_completed", true).apply()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginClick = { _, _ -> 
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onGoogleClick = { /* Handle Google Login */ },
                        onSignUpClick = { navController.navigate(Screen.Signup.route) },
                        onForgotPasswordClick = { /* Handle Forgot Password */ }
                    )
                }

                composable(Screen.Signup.route) {
                    SignupScreen(
                        onSignupClick = { _, _, _ -> 
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        },
                        onGoogleClick = { /* Handle Google Login */ },
                        onSignInClick = { navController.navigate(Screen.Login.route) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onStyleClick = { styleId ->
                            navController.navigate(Screen.StyleDetails.createRoute(styleId))
                        },
                        onSeeAllClick = { category ->
                            navController.navigate(Screen.StylesGrid.createRoute(category))
                        },
                        onProfileClick = { navController.navigate(Screen.Profile.route) }
                    )
                }

                composable(
                    route = Screen.StylesGrid.route,
                    arguments = listOf(
                        navArgument("category") {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category")
                    val gridViewModel: StylesGridViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return StylesGridViewModel(styleRepository, category) as T
                            }
                        }
                    )
                    StylesGridScreen(
                        viewModel = gridViewModel,
                        onBackClick = { navController.popBackStack() },
                        onStyleClick = { styleId ->
                            navController.navigate(Screen.StyleDetails.createRoute(styleId))
                        }
                    )
                }

                composable(
                    route = Screen.StyleDetails.route,
                    arguments = listOf(
                        navArgument("styleId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val styleId = backStackEntry.arguments?.getString("styleId") ?: ""
                    val detailsViewModel: StyleDetailsViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return StyleDetailsViewModel(styleRepository, styleId) as T
                            }
                        }
                    )
                    StyleDetailsScreen(
                        viewModel = detailsViewModel,
                        onBackClick = { navController.popBackStack() },
                        onTryStyleClick = { id -> 
                            navController.navigate(Screen.SelectPhotos.route) 
                            // In a real app, we might pass the styleId to SelectPhotos
                        }
                    )
                }

                composable(Screen.SelectPhotos.route) {
                    SelectPhotosScreen(
                        onBackClick = { navController.popBackStack() },
                        onContinueClick = { navController.navigate(Screen.Generation.route) }
                    )
                }

                composable(Screen.Generation.route) {
                    GenerationScreen(
                        progress = 0.65f, // Mock progress
                        onCancelClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Result.route) {
                    ResultScreen(
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { /* Handle Save */ },
                        onShareClick = { /* Handle Share */ },
                        onGenerateAgainClick = { navController.navigate(Screen.SelectPhotos.route) }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onLogoutClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onSettingsClick = { /* Handle Settings */ },
                        onNavigateToMyGenerations = { navController.navigate(Screen.History.route) }
                    )
                }
                
                composable(Screen.Explore.route) { /* Explore Screen placeholder */ }
                composable(Screen.History.route) { /* History Screen placeholder */ }
                composable(Screen.Create.route) { /* Direct Create route if needed */ }
            }
        }
    }
}
