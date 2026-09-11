package com.icure.cardinal.compose.multiplatform.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.icure.cardinal.compose.multiplatform.ui.screens.auth.AuthErrorScreen
import com.icure.cardinal.compose.multiplatform.ui.screens.auth.LoginScreen
import com.icure.cardinal.compose.multiplatform.ui.screens.auth.SolvingChallengeScreen
import com.icure.cardinal.compose.multiplatform.ui.screens.auth.ValidationScreen
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AppViewModel
import com.icure.cardinal.compose.multiplatform.ui.viewmodels.AuthState

/**
 * Routes the authentication states to their screens.
 *
 * Authentication has no back stack: every state is reachable only from the one
 * before it, and going back means changing the state, not popping. Each transition
 * therefore clears the stack rather than pushing onto it.
 */
@Composable
fun AuthNavGraph(
    appViewModel: AppViewModel
) {
    val navController = rememberNavController()
    val appState by appViewModel.authState.collectAsState()
    val currentEntry by navController.currentBackStackEntryAsState()

    val destination = appState.destination()

    // Keyed on the destination, not on the state: `Unauthenticated` is re-emitted on
    // every keystroke and `SolvingChallenge` on every progress tick, and navigating
    // again on each of those would tear the current screen down mid-typing.
    //
    // The second key is the graph itself: until the NavHost below has set it there is
    // no back stack to read or navigate, so the first pass waits for one to appear.
    val graphReady = currentEntry != null
    LaunchedEffect(destination, graphReady) {
        if (destination == null || !graphReady) return@LaunchedEffect
        val alreadyThere = currentEntry?.destination?.hasRoute(destination::class) == true
        if (alreadyThere) return@LaunchedEffect

        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
        composable<Screen.Login> {
            LoginScreen(appViewModel = appViewModel)
        }

        composable<Screen.SolvingChallenge> {
            SolvingChallengeScreen(appViewModel = appViewModel)
        }

        composable<Screen.AuthError> {
            AuthErrorScreen(appViewModel = appViewModel)
        }

        composable<Screen.Validation> {
            ValidationScreen(appViewModel = appViewModel)
        }
    }
}

/**
 * The screen an authentication state belongs on.
 *
 * `Authenticated` returns `null`: `App.kt` swaps this whole graph out for the
 * signed-in UI, so there is nothing for the graph to navigate to.
 */
private fun AuthState.destination(): Screen? = when (this) {
    is AuthState.Unauthenticated -> Screen.Login
    is AuthState.SolvingChallenge -> Screen.SolvingChallenge
    is AuthState.Error.StartAuthentication -> Screen.AuthError
    is AuthState.PendingCompletion -> Screen.Validation
    is AuthState.Authenticated -> null
}
