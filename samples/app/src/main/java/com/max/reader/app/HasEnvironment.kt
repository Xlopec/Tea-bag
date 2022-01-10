package com.max.reader.app

import com.oliynick.max.reader.app.AppState
import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.message.Message
import kotlinx.coroutines.flow.Flow

interface HasEnvironment {
    val closeCommands: Flow<CloseApp>
    val component: (Flow<Message>) -> Flow<AppState>
}