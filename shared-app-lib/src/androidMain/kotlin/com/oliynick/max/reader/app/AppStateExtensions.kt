package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

actual fun AppState.popScreen(): UpdateWith<AppState, Command> {
    val screen = screen

    return if (screen is TabScreen) {
        if (screen.screens.isEmpty()) {
            this command CloseApp
        } else {
            updateTopScreen { screen.pop() }.noCommand()
        }
    } else {
        dropTopScreen().noCommand()
    }
}