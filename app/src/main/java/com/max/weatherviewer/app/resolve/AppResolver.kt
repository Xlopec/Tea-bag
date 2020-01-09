package com.max.weatherviewer.app.resolve

import com.max.weatherviewer.app.Command
import com.max.weatherviewer.app.Message

interface AppResolver<Env> {

    suspend fun Env.resolve(command: Command): Set<Message>

}