@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import protocol.ClientMessage
import protocol.ComponentId
import protocol.ServerMessage
import java.util.*

fun Gson(
    config: GsonBuilder.() -> Unit = {}
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
