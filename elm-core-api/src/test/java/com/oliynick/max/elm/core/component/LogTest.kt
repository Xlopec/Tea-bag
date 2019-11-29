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

package com.oliynick.max.elm.core.component

import com.oliynick.max.elm.core.actor.Component
import core.component.InterceptData
import core.misc.throwingResolver
import core.scope.runBlockingInTestScope
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import strikt.api.expectThat
import strikt.assertions.containsExactly

@RunWith(RobolectricTestRunner::class)
class LogTest {

    @Test
    @Ignore("will be fixed when api becomes stable")
    fun androidLogger() = runBlockingInTestScope {
        val (formatter, sink) = spyFormatter()

        Component<String, String, String>("", ::throwingResolver, { m, _ -> m.noCommand() }) {
            interceptor = androidLogger("Test", formatter)
        }
            .also { component -> /* modify state */ component("a", "b").first() }

        expectThat(sink).containsExactly(InterceptData("a", "", "a", emptySet()),
                                         InterceptData("b", "a", "b", emptySet()))
    }
}

private fun spyFormatter(): Pair<Formatter<String, String, String>, List<InterceptData>> {
    val sink = mutableListOf<InterceptData>()

    return { message: String, prevState: String, newState: String, commands: Set<String> ->
        sink += InterceptData(message, prevState, newState, commands)
        simpleFormatter<String, String, String>()(message, prevState, newState, commands)
    } to sink
}