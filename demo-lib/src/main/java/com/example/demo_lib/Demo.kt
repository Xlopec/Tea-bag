package com.example.demo_lib

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.Initializer
import com.oliynick.max.tea.core.component.noCommand
import com.oliynick.max.tea.core.debug.component.Component
import com.oliynick.max.tea.core.debug.gson.GsonSerializer
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {

    val component = Component<String, String, String, JsonElement>(
        ComponentId("test id"),
        Initializer("init"),
        { emptySet() },
        { _, s -> s.noCommand() },
        GsonSerializer { },
        this,
    )

    component(flowOf("a", "b", "c"))
        .map { s -> "New snapshot $s" }
        .collect(::println)
}
