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
import kotlinx.collections.immutable.PersistentList

@DslMarker
private annotation class NavigationStackMutatorScope

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
 * Mutates a navigation stack by applying [block] to it. Returns the updated stack
 * and any commands produced by the mutators in [block].
 */
public fun <I : Any, E : NavStackEntry<I>, C> NavigationStack<E>.mutate(
    block: MutatorScope<I, E, C>.() -> Unit,
): Update<NavigationStack<E>, C> {
    val builder = value.builder()
    val commands = MutatorScope<I, E, C>(builder)
        .apply(block)
        .commands

    return NavigationStack(builder.build()) command commands
}

/**
 * Scope handed to [mutate]'s block. Holds the mutable working copy of the stack
 * and a buffer for commands produced during mutation. Mutation operations live
 * as extensions; only the underlying state is held here.
 */
@NavigationStackMutatorScope
public class MutatorScope<I : Any, E : NavStackEntry<I>, C> internal constructor(
    builder: PersistentList.Builder<E>,
) {
    /** Commands buffered by the operations applied to this scope. */
    public val commands: MutableSet<C> = mutableSetOf()

    /** The mutable stack itself. Operations append to / remove from this list. */
    public val mutator: MutableList<E> = builder
}

/**
 * Pushes [entry] onto the stack.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.push(entry: E) {
    mutator.add(entry)
}

/**
 * Pushes the entry produced by [init] and records its commands.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.push(init: Update<E, C>) {
    mutator.add(init.first)
    commands += init.second
}

/**
 * Removes and returns the top entry.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.pop(): E =
    mutator.removeAt(mutator.lastIndex)

/**
 * Removes ALL entries matching [predicate] and records commands produced by [onPop]
 * for each removed entry.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.popAll(
    predicate: (E) -> Boolean,
    onPop: (E) -> Set<C>,
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
 * in order of removal (top first).
 *
 * The check runs against the entry *currently* on top before each potential pop, so calling
 * `popUntil { it is Home }` from `[Home, Details, Filters]` removes `[Filters, Details]` and
 * leaves `[Home]`.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.popUntil(
    predicate: (E) -> Boolean,
): List<E> {
    val popped = mutableListOf<E>()
    while (mutator.size > 1 && !predicate(mutator.last())) {
        popped += mutator.removeAt(mutator.lastIndex)
    }
    return popped
}

/**
 * Pops entries from the top of the stack until an entry with [id] is the new top, or until
 * the stack has only one entry (which is never popped). Returns the removed entries.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.popTo(id: I): List<E> =
    popUntil { it.id == id }

/**
 * Replaces the top entry with [entry] and returns the removed top.
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.replaceTop(entry: E): E {
    val popped = mutator.removeAt(mutator.lastIndex)
    mutator.add(entry)
    return popped
}

/**
 * Clears the entire stack and pushes [entry] as the new root. Returns the removed entries
 * in their original order (bottom first).
 */
public fun <I : Any, E : NavStackEntry<I>, C> MutatorScope<I, E, C>.clearAndPush(entry: E): List<E> {
    val removed = mutator.toList()
    mutator.clear()
    mutator.add(entry)
    return removed
}

/**
 * Modifies ***ALL*** entries that are instances of [S] using [updater] and records
 * the produced commands.
 */
public inline fun <I : Any, E : NavStackEntry<I>, C, reified S : E> MutatorScope<I, E, C>.updateInstanceOf(
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
public inline fun <I : Any, E : NavStackEntry<I>, C, reified S : E> MutatorScope<I, E, C>.updateInstanceOfById(
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
public fun <I : Any, E : NavStackEntry<I>, C, T> MutatorScope<I, E, C>.switchToTab(
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

private fun <T> MutableList<T>.swap(
    i: Int,
    j: Int,
) {
    if (i == j) return

    val tmp = this[j]

    set(j, this[i])
    set(i, tmp)
}
