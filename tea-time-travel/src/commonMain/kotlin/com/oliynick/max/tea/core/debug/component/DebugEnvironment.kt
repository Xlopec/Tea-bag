/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.component

import io.github.xlopec.tea.core.Env
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.JsonSerializer
import com.oliynick.max.tea.core.debug.session.DebugSession
import com.oliynick.max.tea.core.debug.session.SessionFactory
import io.ktor.http.*

/**
 * Same as [environment][Env] but with extra settings
 *
 * @param env see [environment][Env]
 * @param settings server settings to use
 * @param M message type
 * @param S state type
 * @param C command type
 * @param J json tree type
 */
public data class DebugEnv<M, S, C, J>(
    val env: Env<M, S, C>,
    val settings: Settings<M, S, J>
)

/**
 * Holds server settings such as component identifier,
 * json serializer, debug server url and so on
 *
 * @param id component identifier
 * @param url debug server url
 * @param serializer json serializer
 * @param sessionFactory a function that for a given server settings creates a new [debug session][DebugSession]
 * @param M message type
 * @param S state type
 * @param J json tree type
 */
public data class Settings<M, S, J>(
    val id: ComponentId,
    val serializer: JsonSerializer<J>,
    val url: Url,
    val sessionFactory: SessionFactory<M, S, J>
)
