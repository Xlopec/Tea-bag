@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oliynick.max.tea.core.debug.protocol.*
import java.util.*

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
