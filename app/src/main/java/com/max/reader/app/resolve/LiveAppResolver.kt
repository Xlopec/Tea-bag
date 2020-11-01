@file:Suppress("FunctionName")

package com.max.reader.app.resolve

import com.max.reader.app.*
import com.max.reader.screens.feed.resolve.FeedResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.channels.BroadcastChannel

@Deprecated("wait until it'll be fixed")
fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : FeedResolver<Env> = object :
    AppResolver<Env> {
    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> close(command)
            is FeedCommand -> resolve(command)
        }

    suspend fun Env.close(
        command: CloseApp
    ): Set<Message> = command.sideEffect { closeCommands.offer(command) }
}

interface LiveAppResolver<Env> : AppResolver<Env> where Env : HasCommandTransport,
                                                        Env : FeedResolver<Env> {

    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> close(command)
            is FeedCommand -> resolve(command)
        }

    suspend fun Env.close(
        command: CloseApp
    ): Set<Message> = command.sideEffect { closeCommands.offer(command) }

}

interface HasCommandTransport {
    val closeCommands: BroadcastChannel<CloseApp>
}

fun CommandTransport() = object : HasCommandTransport {
    override val closeCommands: BroadcastChannel<CloseApp> = BroadcastChannel(1)
}
