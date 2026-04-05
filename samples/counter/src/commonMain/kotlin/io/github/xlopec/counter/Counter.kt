/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

@file:OptIn(ExperimentalTeaApi::class)

package io.github.xlopec.counter

import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.ExperimentalTeaApi
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.sideEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**Async initializer, provides initial state*/
suspend fun initializer(): Initial<Int, Int> = Initial(0)

/**Some tracker*/
context(_: Sink<Int>, scope: CoroutineScope)
fun track(
    events: Flow<Snapshot<Int, Int, Int>>,
) {
    scope.launch {
        events.collect { event -> sideEffect { println("Track: \"$event\"") } }
    }
}

/**App logic, for now it just adds delta to count and returns this as result*/
fun add(
    delta: Int,
    counter: Int,
): Update<Int, Int> = (counter + delta) command delta

/**Some UI, e.g. console*/
suspend fun display(
    snapshot: Snapshot<*, *, *>,
) {
    println("Display: $snapshot")
}

fun main() = runBlocking {
    // Somewhere at the application level
    val component = Component(
        initializer = ::initializer,
        resolver = { snapshot -> track(snapshot) },
        updater = ::add,
        scope = this,
    )
    // UI = component([message1, message2, ..., message N])
    component(+1, +2, -3).collect(::display)
}
