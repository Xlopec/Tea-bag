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

import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.noCommand
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
            { message, _ -> SomeTestState(message.str.copy(value = message.str.value + Random.nextDouble().toString()))
                .noCommand() },
            androidLogger("Test")
        ).also {
    //        it.invoke(SomeTestCommand(SomeTestString("hello"), listOf(1234))).first()
        }

    }
}