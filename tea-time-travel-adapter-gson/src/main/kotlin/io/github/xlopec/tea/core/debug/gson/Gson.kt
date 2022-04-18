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

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.github.xlopec.tea.core.debug.protocol.ApplyMessage
import io.github.xlopec.tea.core.debug.protocol.ApplyState
import io.github.xlopec.tea.core.debug.protocol.ClientMessage
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentAttached
import io.github.xlopec.tea.core.debug.protocol.NotifyComponentSnapshot
import io.github.xlopec.tea.core.debug.protocol.NotifyServer
import io.github.xlopec.tea.core.debug.protocol.ServerMessage
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.core.debug.gson.TypeAppenderAdapterFactory

/**
 * Type alias a for Gson specific [ClientMessage]
 */
public typealias GsonClientMessage = ClientMessage<JsonElement>
/**
 * Type alias a for Gson specific [ApplyMessage]
 */
public typealias GsonApplyMessage = ApplyMessage<JsonElement>
/**
 * Type alias a for Gson specific [ApplyState]
 */
public typealias GsonApplyState = ApplyState<JsonElement>
/**
 * Type alias a for Gson specific [ServerMessage]
 */
public typealias GsonServerMessage = ServerMessage<JsonElement>
/**
 * Type alias a for Gson specific [NotifyComponentSnapshot]
 */
public typealias GsonNotifyComponentSnapshot = NotifyComponentSnapshot<JsonElement>
/**
 * Type alias a for Gson specific [NotifyServer]
 */
public typealias GsonNotifyServer = NotifyServer<JsonElement>
/**
 * Type alias a for Gson specific [NotifyComponentAttached]
 */
public typealias GsonNotifyComponentAttached = NotifyComponentAttached<JsonElement>

/**
 * Configures and creates a new [Gson] instance using supplied lambda with
 * preconfigured adapters installed and null serialization enabled.
 *
 * This function is a preferred way to use [Gson] in couple with debuggable component
 * as the resulting json will have all needed meta information appended that in turn
 * is needed by the debug plugin to work correctly
 *
 * @see TypeAppenderAdapterFactory
 * @param config configuration block
 */
public fun Gson(
    config: GsonBuilder.() -> Unit = {},
): Gson =
    GsonBuilder()
        .serializeNulls()
        .registerTypeHierarchyAdapter(ServerMessage::class.java, ServerMessageAdapter)
        .registerTypeHierarchyAdapter(ClientMessage::class.java, ClientMessageAdapter)
        .registerTypeAdapter(UUID::class.java, UUIDAdapter)
        .registerTypeAdapter(ComponentId::class.java, ComponentIdAdapter)
        .registerTypeAdapterFactory(TypeAppenderAdapterFactory)
        .apply(config)
        .create()
