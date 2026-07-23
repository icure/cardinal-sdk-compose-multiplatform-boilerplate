package com.icure.cardinal.compose.multiplatform.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.icure.cardinal.compose.multiplatform.ui.screens.LoginScreen
import com.icure.cardinal.compose.multiplatform.ui.screens.ValidationScreen
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppState

@Composable
fun AuthNavGraph(
    appViewModel: AppViewModel
) {
    val navController = rememberNavController()
    val appState by appViewModel.authState.collectAsState()

    // Navigate based on auth state changes within the auth flow
    LaunchedEffect(appState) {
        when (appState) {
            is AppState.Unauthenticated -> {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AppState.PendingValidation -> {
                navController.navigate(Screen.Validation) {
                    popUpTo(Screen.Login) { inclusive = true }
                }
            }
            is AppState.Authenticated -> {
                // Do nothing - App.kt will handle showing WelcomeScreen
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
        composable<Screen.Login> {
            LoginScreen(
                appViewModel = appViewModel
            )
        }

        composable<Screen.Validation> {
            ValidationScreen(
                appViewModel = appViewModel
            )
        }
    }
}

