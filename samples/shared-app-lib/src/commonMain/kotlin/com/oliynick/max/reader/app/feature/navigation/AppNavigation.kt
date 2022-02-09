package com.oliynick.max.reader.app.feature.navigation

import com.oliynick.max.reader.app.AppState
import com.oliynick.max.reader.app.Navigation
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.tea.core.component.UpdateWith

fun interface AppNavigation {

    fun navigate(
        nav: Navigation,
        state: AppState,
    ): UpdateWith<AppState, Command>
}