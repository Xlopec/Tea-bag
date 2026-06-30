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

package io.github.xlopec.tea.navigation

import androidx.compose.ui.util.fastForEachIndexed
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command

/**
 * Returns the current screen to draw (the top of the stack).
 */
public inline val <T> NavigationStack<T>.screen: T
    get() = last()

/**
 * The entry immediately below [screen], or `null` if the stack has only one entry.
 * Convenient default for `previousScreenFor` in [PredictiveBackContainer].
 */
public inline val <T> NavigationStack<T>.previousScreen: T?
    get() = getOrNull(lastIndex - 1)

/**
 * Mutates a navigation stack by applying [block]. The block receives the stack's mutable
 * working copy and a command-buffer as context parameters, so the mutator operations
 * (`push`, `pop`, …) are callable as plain functions.
 *
 * Returns the updated stack and any commands produced during the block.
 */
public fun <E : NavStackEntry<*>, C> NavigationStack<E>.mutate(
    block: context(MutableList<E>, MutableSet<C>) () -> Unit,
): Update<NavigationStack<E>, C> {
    val builder = value.builder()
    val commands = mutableSetOf<C>()
    block(builder, commands)
    return NavigationStack(builder.build()) command commands
}

/**
 * Pushes [entry] onto the stack. Any [cmds] are appended to the command buffer.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public fun <E, C> push(entry: E, cmds: Set<C> = emptySet()) {
    mutator.add(entry)
    commands += cmds
}

/**
 * Pushes the entry produced by [init] and records its commands.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public fun <E, C> push(init: Update<E, C>) {
    mutator.add(init.first)
    commands += init.second
}

/**
 * Removes and returns the top entry. [onPop] runs against the removed entry and its
 * commands are appended to the buffer; defaults to producing none.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public inline fun <E, C> pop(onPop: (E) -> Set<C> = { emptySet() }): E {
    val popped = mutator.removeAt(mutator.lastIndex)
    commands += onPop(popped)
    return popped
}

/**
 * Removes ALL entries matching [predicate]. [onPop] runs against each removed entry.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public inline fun <E, C> popAll(
    crossinline predicate: (E) -> Boolean,
    crossinline onPop: (E) -> Set<C> = { emptySet() },
) {
    mutator.removeAll { entry ->
        val shouldRemove = predicate(entry)
        if (shouldRemove) {
            commands += onPop(entry)
        }
        shouldRemove
    }
}

/**
 * Pops entries from the top of the stack until [predicate] returns `true` for the new top,
 * or until the stack has only one entry (which is never popped). Returns the removed entries
 * in order of removal (top first). [onPop] runs against each removed entry.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public inline fun <E, C> popUntil(
    predicate: (E) -> Boolean,
    onPop: (E) -> Set<C> = { emptySet() },
): List<E> {
    val popped = mutableListOf<E>()
    while (mutator.size > 1 && !predicate(mutator.last())) {
        val entry = mutator.removeAt(mutator.lastIndex)
        commands += onPop(entry)
        popped += entry
    }
    return popped
}

/**
 * Pops entries from the top until an entry with [id] is the new top, or until the stack
 * has only one entry (which is never popped). [onPop] runs against each removed entry.
 */
context(_: MutableList<E>, _: MutableSet<C>)
public fun <I : Any, E : NavStackEntry<I>, C> popTo(
    id: I,
    onPop: (E) -> Set<C> = { emptySet() },
): List<E> = popUntil({ it.id == id }, onPop)

/**
 * Replaces the top entry with [entry], appends [cmds] to the buffer, and returns the
 * removed top.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public fun <E, C> replaceTop(entry: E, cmds: Set<C> = emptySet()): E {
    val popped = mutator.removeAt(mutator.lastIndex)
    mutator.add(entry)
    commands += cmds
    return popped
}

/**
 * Clears the entire stack, pushes [entry] as the new root, and appends [cmds] to the buffer.
 * Returns the removed entries in their original order (bottom first).
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public fun <E, C> clearAndPush(entry: E, cmds: Set<C> = emptySet()): List<E> {
    val removed = mutator.toList()
    mutator.clear()
    mutator.add(entry)
    commands += cmds
    return removed
}

/**
 * Modifies ***ALL*** entries that are instances of [S] using [updater] and records
 * the produced commands.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public inline fun <E, C, reified S : E> updateInstanceOf(
    updater: (S) -> Update<E, C>,
) {
    mutator.fastForEachIndexed { i, entry ->
        if (entry is S) {
            val (upd, cmd) = updater(entry)
            mutator[i] = upd
            commands += cmd
        }
    }
}

/**
 * Modifies the first entry that is an instance of [S] with [id] using [updater] and records
 * the produced commands.
 */
context(mutator: MutableList<E>, commands: MutableSet<C>)
public inline fun <I : Any, E : NavStackEntry<I>, C, reified S : E> updateInstanceOfById(
    id: I,
    updater: S.() -> Update<S, C>,
) {
    val index = mutator.indexOfFirst { it.id == id && it is S }.takeIf { it >= 0 } ?: return
    @Suppress("UNCHECKED_CAST")
    val (upd, cmd) = updater(mutator[index] as S)

    mutator[index] = upd
    commands += cmd
}

/**
 * Switches the navigation stack to a given [tab]. To maintain proper tab navigation,
 * the stack must contain tabs in the following order:
 *
 * `[H, h1, h2, ... hi, A, a1, a2, a3, ... ai]`
 *
 * Where `H` and `A` are tab roots, and `h1 ... hi` and `a1 ... ai` are child tab entries.
 * The sequence `[H, h1, ... , hi]` forms a tab group — H.
 *
 * **Note:** such groups can't be mixed. For example, the following sequence:
 *
 * `[H, h2, A, h1, a3, a2, a1]`
 *
 * is prohibited.
 *
 * #### Example:
 * A user switches to tab group H, given the stack state is `[H, h1, h2, ... hi, A, a1, a2, a3, ... ai]`.
 * After the tab switch, the stack's state becomes `[A, a1, a2, a3, ... ai, H, h1, h2, ... hi]`.
 *
 * @param T tab type
 * @param tab tab to switch to
 * @param belongsToTab predicate to check if a stack entry [E] belongs to a tab [T]
 */
context(mutator: MutableList<E>, _: MutableSet<*>)
public inline fun <E, T> switchToTab(
    tab: T,
    belongsToTab: (E, T) -> Boolean,
) {
    // complexity O(n * m + (m - n)) where n - group size, m - list size
    // move each screen that belongs to given group to the end
    var lo = 0
    var hi = mutator.size

    while (lo < hi) {
        if (belongsToTab(mutator[lo], tab)) {
            // we found screen that belongs to the group,
            // swap it with adjacent screen until end of list is reached
            var j = lo
            while (j < mutator.lastIndex) {
                mutator.swap(j, j + 1)
                j++
            }
            // now we have one less screen to proceed,
            // decrease [hi] to keep it untouched
            hi--
        } else {
            // we cannot proceed until we move the group,
            // so we stay on the same index
            lo++
        }
    }
}

@PublishedApi
internal fun <T> MutableList<T>.swap(
    i: Int,
    j: Int,
) {
    if (i == j) return

    val tmp = this[j]

    set(j, this[i])
    set(i, tmp)
}
