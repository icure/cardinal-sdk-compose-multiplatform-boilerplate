package com.icure.cardinal.compose.multiplatform.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Validation : Screen
}

