package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.theme

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener

internal class ThemeChangeListener(
    val updateColors: () -> Unit
) : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        updateColors()
    }
}

