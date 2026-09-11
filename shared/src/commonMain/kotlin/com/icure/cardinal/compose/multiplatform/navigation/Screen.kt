package com.icure.cardinal.compose.multiplatform.navigation

import kotlinx.serialization.Serializable

/** The destinations of the authentication graph, one per pre-authenticated state. */
sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object SolvingChallenge : Screen

    @Serializable
    data object AuthError : Screen

    @Serializable
    data object Validation : Screen
}
