/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")
@file:OptIn(ExperimentalTeaApi::class)

package com.oliynick.max.tea.core.debug.component

import com.oliynick.max.tea.core.Env
import com.oliynick.max.tea.core.ExperimentalTeaApi
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonConverter
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.SessionBuilder
import io.ktor.http.*

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
public data class DebugEnv<M, S, C, J>(
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
public data class ServerSettings<M, S, J>(
    val id: ComponentId,
    val serializer: JsonConverter<J>,
    val url: Url,
    val sessionBuilder: SessionBuilder<M, S, J>
)
