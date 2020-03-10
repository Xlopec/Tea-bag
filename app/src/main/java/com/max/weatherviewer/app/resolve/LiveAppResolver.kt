@file:Suppress("FunctionName")

package com.max.weatherviewer.app.resolve

import com.max.weatherviewer.app.CloseApp
import com.max.weatherviewer.app.Command
import com.max.weatherviewer.app.FeedCommand
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.screens.feed.resolve.FeedResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.channels.BroadcastChannel

fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : FeedResolver<Env> = object :
        LiveAppResolver<Env> {}

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
