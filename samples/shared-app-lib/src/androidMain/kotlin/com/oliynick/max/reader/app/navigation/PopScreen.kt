package com.oliynick.max.reader.app.navigation

import com.oliynick.max.reader.app.AppState
import com.oliynick.max.reader.app.TabScreen
import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.screen
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