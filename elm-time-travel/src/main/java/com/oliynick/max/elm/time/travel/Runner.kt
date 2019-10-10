package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.invoke
import com.oliynick.max.elm.core.component.noCommand
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {

        component<SomeTestCommand, String, SomeTestState>(
            Settings(ComponentId("webSocketComponent")),
            SomeTestState(SomeTestString("initial")),
            { emptySet() },
            { message, _ -> SomeTestState(message.str).noCommand() },
            androidLogger("Test")
        ).also {
            it(SomeTestCommand(SomeTestString("hello"), listOf(1234))).first()
        }

    }
}