package io.github.xlopec.reader.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun rememberWindowInsetsController(): WindowInsetsControllerCompat {
    val view = LocalView.current

    return remember(view) {
        val window = view.findWindow()
        WindowCompat.getInsetsController(window, window.decorView)
    }
}

private fun View.findWindow(): Window =
    (parent as? DialogWindowProvider)?.window ?: context.findWindow()

private tailrec fun Context.findWindow(): Window =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.findWindow()
        else -> error("No window found")
    }
