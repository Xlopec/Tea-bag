/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.invoke
import com.oliynick.max.elm.core.component.noCommand
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import protocol.ComponentId
import kotlin.random.Random

data class SomeTestString(val value: String)

data class SomeTestCommand(val str: SomeTestString/*, val collection : Collection<Any>*/)

data class SomeTestState(val string: SomeTestString/*, val uri: Uri*/)

fun main() {

    runBlocking {

        val dependencies = DebugEnv(
            Env<SomeTestCommand, String, SomeTestState>(
                SomeTestState(SomeTestString("initial")),
                { emptySet() },
                { message, _ ->
                    println(message)
                    SomeTestState(message.str.copy(value = message.str.value + Random.nextDouble().toString())).noCommand()
                }
            ),
            ServerSettings(
                ComponentId("webSocketComponent"),
                gsonSerializer(),
                URL()
            ),
            ::WebSocketSession
        )

        Component(dependencies)
            .also {
                launch {
                    it.invoke(
                        SomeTestCommand(SomeTestString("hello")/*, listOf(1234)*/),
                        SomeTestCommand(SomeTestString("Suck")/*, listOf(24, 43, 13, "loh")*/),
                        SomeTestCommand(SomeTestString("dfvbdf")/*, listOf(24, 43, 13, "sdfgsd")*/)
                    ).collect()
                }
            }

    }
}