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
@file:OptIn(UnstableApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.UnstableApi
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import java.net.URL

/**
 * Same as [environment][Env] but with extra settings
 *
 * @param componentEnv see [environment][Env]
 * @param serverSettings server settings to use
 * @param M message type
 * @param S state type
 * @param C command type
 * @param J json tree type
 */
data class DebugEnv<M, S, C, J>(
    inline val componentEnv: Env<M, S, C>,
    inline val serverSettings: ServerSettings<M, S, J>
)

/**
 * Holds server settings such as component identifier,
 * json serializer, debug server url and so on
 *
 * @param id component identifier
 * @param url debug server url
 * @param serializer json serializer
 * @param sessionBuilder a function that for a given server settings creates a new [debug session][DebugSession]
 * @param M message type
 * @param S state type
 * @param J json tree type
 */
data class ServerSettings<M, S, J>(
    val id: ComponentId,
    val serializer: JsonConverter<J>,
    val url: URL,
    val sessionBuilder: SessionBuilder<M, S, J>
)
