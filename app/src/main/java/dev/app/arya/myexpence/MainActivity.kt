package dev.app.arya.myexpence

import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import dev.app.arya.myexpence.ui.theme.MyExpenceTheme
import dev.app.arya.myexpence.ui.AppRoot
import dev.app.arya.myexpence.ui.LocalAppContainer

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MyExpenceApp
        val container = app.container

        setContentView(
            ComposeView(this).apply {
                setContent {
                    MyExpenceTheme {
                        CompositionLocalProvider(LocalAppContainer provides container) {
                            LaunchedEffect(Unit) {
                                container.categoryRepository.seedDefaultsIfEmpty(System.currentTimeMillis())
                            }
                            AppRoot()
                        }
                    }
                }
            }
        )
    }
}