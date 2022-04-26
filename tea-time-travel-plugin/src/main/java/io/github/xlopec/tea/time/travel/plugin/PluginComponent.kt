/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.trace
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Interceptor
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.with
import io.github.xlopec.tea.time.travel.plugin.misc.PluginId
import io.github.xlopec.tea.time.travel.plugin.misc.settings
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.plugin.model.state.Stopped
import kotlinx.coroutines.Dispatchers.IO
import com.intellij.openapi.diagnostic.Logger as PlatformLogger

fun PluginComponent(
    environment: Environment,
    properties: PropertiesComponent,
): Component<Message, State, Command> =
    Component<Message, Command, State>(
        initializer = AppInitializer(properties),
        resolver = { c, ctx -> with(environment) { resolve(c, ctx) } },
        updater = { m, s -> with(environment) { update(m, s) } },
        scope = environment
    ).with(Logger(PlatformLogger.getInstance(PluginId)))

private fun AppInitializer(
    properties: PropertiesComponent
): Initializer<State, Command> =
    Initializer(IO) { Initial(Stopped(properties.settings), emptySet()) }

private fun Logger(
    logger: PlatformLogger
): Interceptor<Message, State, Command> =
    { snapshot ->
        logger.info(snapshot.infoMessage)
        logger.debug { snapshot.debugMessage }
        logger.trace { snapshot.traceMessage }
    }

private val Snapshot<*, *, *>.infoMessage: String
    get() = when (this) {
        is Initial -> "Init class=${currentState?.javaClass}, commands=${commands.size}"
        is Regular -> """
        Regular with new state=${currentState?.javaClass},
        previousState state=${previousState?.javaClass},
        caused by message=${message?.javaClass},
        commands=${commands.size}"}
    """.trimIndent()
    }

private val Snapshot<*, *, *>.debugMessage: String
    get() = when (this) {
        is Initial -> "Init class=${currentState?.javaClass}" +
                if (commands.isEmpty()) "" else ", commands=${commands.joinToString { it?.javaClass.toString() }}"
        is Regular -> """
        Regular with new state=${currentState?.javaClass},
        prev state=${previousState?.javaClass},
        caused by message=${message?.javaClass}
        ${if (commands.isEmpty()) "" else "\ncommands=${commands.joinToString { it?.javaClass.toString() }}"}
    """.trimIndent()
    }

private val Snapshot<*, *, *>.traceMessage: String
    get() = when (this) {
        is Initial -> "Init with state=$currentState" +
                if (commands.isEmpty()) "" else ", commands=$commands"
        is Regular -> """
        Regular with new state=$currentState,
        previousState state=$previousState,
        caused by message=$message
        ${if (commands.isEmpty()) "" else "\ncommands=$commands"}
    """.trimIndent()
    }
