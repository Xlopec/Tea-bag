package com.oliynick.max.tea.core.debug.app.presentation.ui.theme

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener

internal class ThemeChangeListener(
    val updateColors: () -> Unit
) : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        updateColors()
    }
}

