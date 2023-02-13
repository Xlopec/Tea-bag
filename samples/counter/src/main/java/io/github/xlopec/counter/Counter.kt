@file:OptIn(ExperimentalTeaApi::class)

package io.github.xlopec.counter

import io.github.xlopec.tea.core.*
import kotlinx.coroutines.runBlocking

/**Async initializer, provides initial state*/
suspend fun initializer(): Initial<Int, Int> = Initial(0)

/**Some tracker*/
fun track(
    event: Snapshot<Int, Int, Int>,
    ctx: ResolveCtx<Int>,
) {
    ctx sideEffect { println("Track: \"$event\"") }
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
        resolver = ::track,
        updater = ::add,
        scope = this,
    )
    // UI = component([message1, message2, ..., message N])
    component(+1, +2, -3).collect(::display)
}
