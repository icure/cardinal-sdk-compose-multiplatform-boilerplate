package com.icure.cardinal.compose.multiplatform

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CardinalComposeMultiplatform",
    ) {
        App()
    }
}