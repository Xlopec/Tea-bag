package com.max.weatherviewer.app.update

import com.max.weatherviewer.app.Command
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.State
import com.oliynick.max.tea.core.component.UpdateWith

interface AppUpdater<Env> {

    fun Env.update(
        message: Message,
        state: State
    ): UpdateWith<State, Command>

}