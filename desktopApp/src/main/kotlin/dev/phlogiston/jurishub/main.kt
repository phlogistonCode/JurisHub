package dev.phlogiston.jurishub

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.phlogiston.jurishub.ui.DesktopApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JurisHub",
    ) {
        DesktopApp()
    }
}