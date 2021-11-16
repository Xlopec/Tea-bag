package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

actual fun AppState.popScreen(): UpdateWith<AppState, Command> {
    val screen = screen

    return if (screen is TabScreen) {
        this command CloseApp
    } else {
        copy(screens = screens.pop()).noCommand()
    }
}