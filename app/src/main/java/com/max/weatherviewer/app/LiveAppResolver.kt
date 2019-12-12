@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.CloseApp
import com.max.weatherviewer.Command
import com.max.weatherviewer.FeedCommand
import com.max.weatherviewer.home.FeedResolver
import com.oliynick.max.elm.core.component.sideEffect
import kotlinx.coroutines.channels.Channel

interface AppResolver<Env> {

    suspend fun Env.resolve(command: Command): Set<Message>

}

fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : FeedResolver<Env> = object : LiveAppResolver<Env> {}

interface LiveAppResolver<Env> : AppResolver<Env> where Env : HasCommandTransport,
                                                        Env : FeedResolver<Env> {

    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> command.sideEffect { closeCommands.offer(command) }
            is FeedCommand -> resolve(command)
        }

}

interface HasCommandTransport {
    val closeCommands: Channel<CloseApp>
}

fun CommandTransport() = object : HasCommandTransport {
    override val closeCommands: Channel<CloseApp> = Channel()
}
