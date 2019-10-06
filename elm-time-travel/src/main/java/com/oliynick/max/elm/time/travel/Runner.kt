package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {

        component(
            Settings("webSocketComponent"),
            component<SomeTestCommand, String, SomeTestState>(
                SomeTestState(SomeTestString("initial")),
                { emptySet() },
                { message, state -> SomeTestState(message.str).noCommand() },
                androidLogger("Test")
            )
        )

    }
}