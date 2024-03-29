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

package io.github.xlopec.tea.time.travel.plugin.integration

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.trace
import io.github.xlopec.tea.core.*
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.util.PluginId
import io.github.xlopec.tea.time.travel.plugin.util.settings
import kotlinx.coroutines.Dispatchers.IO

internal val PluginLogger = Logger.getInstance(PluginId)

fun PluginComponent(
    environment: Environment,
    initializer: Initializer<State, Command>,
): Component<Message, State, Command> =
    Component(
        initializer = initializer,
        resolver = { snapshot, ctx -> with(environment) { resolve(snapshot, ctx) } },
        updater = { m, s -> with(environment) { update(m, s) } },
        scope = environment
    ).with(LoggerInterceptor(PluginLogger))

fun AppInitializer(
    properties: PropertiesComponent,
): Initializer<State, Command> =
    Initializer(IO) { Initial(State(properties.settings)) }

private fun LoggerInterceptor(
    logger: Logger,
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
        is Initial ->
            "Init class=${currentState?.javaClass}" +
                    if (commands.isEmpty()) "" else ", commands=${commands.joinToString { it?.javaClass.toString() }}"
        is Regular ->
            """
        Regular with new state=${currentState?.javaClass},
        prev state=${previousState?.javaClass},
        caused by message=${message?.javaClass}
        ${if (commands.isEmpty()) "" else "\ncommands=${commands.joinToString { it?.javaClass.toString() }}"}
    """.trimIndent()
    }

private val Snapshot<*, *, *>.traceMessage: String
    get() = when (this) {
        is Initial ->
            "Init with state=$currentState ${if (commands.isEmpty()) "" else ", commands=$commands}"}"
        is Regular ->
            """
        Regular with new state=$currentState,
        previousState state=$previousState,
        caused by message=$message
        ${if (commands.isEmpty()) "" else "\ncommands=$commands"}
    """.trimIndent()
    }
