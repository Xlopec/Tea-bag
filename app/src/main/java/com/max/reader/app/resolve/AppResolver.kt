package com.max.reader.app.resolve

import com.max.reader.app.Command
import com.max.reader.app.Message

interface AppResolver<Env> {

    suspend fun Env.resolve(command: Command): Set<Message>

}