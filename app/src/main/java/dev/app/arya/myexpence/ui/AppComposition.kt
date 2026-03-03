package dev.app.arya.myexpence.ui

import androidx.compose.runtime.compositionLocalOf
import dev.app.arya.myexpence.data.AppContainer

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}

