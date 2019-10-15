package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.time.travel.protocol.ComponentId
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

data class SomeTestString(val value: String)

data class SomeTestCommand(val str: SomeTestString, val collection : Collection<Any>)

data class SomeTestState(val string: SomeTestString)

fun main() {

    runBlocking {

        component<SomeTestCommand, String, SomeTestState>(
            Settings(ComponentId("webSocketComponent")),
            SomeTestState(SomeTestString("initial")),
            { emptySet() },
            { message, _ -> SomeTestState(message.str.copy(value = message.str.value + Random.nextDouble().toString())).noCommand() },
            androidLogger("Test")
        ).also {
    //        it.invoke(SomeTestCommand(SomeTestString("hello"), listOf(1234))).first()
        }

    }
}