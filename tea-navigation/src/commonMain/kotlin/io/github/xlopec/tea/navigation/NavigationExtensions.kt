package io.github.xlopec.tea.navigation

import androidx.compose.ui.util.fastForEachIndexed
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import kotlinx.collections.immutable.PersistentList

@DslMarker
private annotation class NavigationStackMutatorScope

/**
 * Returns current screen to draw
 */
public inline val <T> NavigationStack<T>.screen: T
    get() = last()

/**
 * Mutates a navigation stack by applying [block] to it
 */
public fun <I : Any, E : NavStackEntry<I>, C> NavigationStack<E>.mutate(
    block: NavigationStackMutator<I, E, C>.() -> Unit,
): Update<NavigationStack<E>, C> {
    val builder = value.builder()
    val commands = NavigationStackMutator<I, E, C>(builder)
        .apply(block)
        .commands

    return NavigationStack(builder.build()) command commands
}

/**
 * Helper class that allows performing mutable operations on [NavigationStack]
 */
@NavigationStackMutatorScope
public class NavigationStackMutator<I : Any, E : NavStackEntry<I>, C> internal constructor(
    builder: PersistentList.Builder<E>,
) {
    /**
     * Commands to be executed after update
     */
    public val commands: MutableSet<C> = mutableSetOf()

    /**
     * Mutable navigation stack to be used after update
     */
    public val mutator: MutableList<E> = builder

    /**
     * Switches navigation stack to a given [tab]. To maintain a proper tab navigation stack must contain tabs in the following order:
     *
     * `[H, h1, h2, ... hi, A, a1, a2, a3, ... ai]`
     *
     * Where `H`, `A` are tab roots, `h1 ... hi` and `a1 ... ai` are child tab entries.
     * Sequence `[H, h1, ... , hi]` forms a tab group - H
     *
     * **Note** such groups can't be mixed, e.g. the following sequence:
     *
     * `[H, h2, A, h1, a3, a2, a1]`
     *
     * is prohibited.
     *
     * #### Example:
     * User switches to tab group H given stack state is `[H, h1, h2, ... hi, A, a1, a2, a3, ... ai]`. After tab switch
     * stack's state becomes `[A, a1, a2, a3, ... ai, H, h1, h2, ... hi]`
     *
     * @param tab tab to switch to
     * @param belongsToTab predicate to check if a stack entry [E] belongs to a tab [T]
     */
    public fun <T> switchToTab(
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
            }
            // we cannot proceed until we move the group,
            // so we stay on the same index
            if (!belongsToTab(mutator[lo], tab)) {
                lo++
            }
        }
    }

    /**
     * Modifies ***ALL*** entries that are instance of [S] using provided [updater]
     * and records produced commands
     *
     * @param updater updater to use
     */
    public inline fun <reified S : E> updateInstanceOf(
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
     * Modifies first entry that is instance of [S] with [id] using provided [updater]
     * and records produced commands
     *
     * @param updater updater to use
     * @param id stack entry identifier
     */
    public inline fun <reified S : E> updateInstanceOfById(
        id: I,
        updater: S.() -> Update<S, C>,
    ) {
        val index = mutator.indexOfFirst { it.id == id && it is S }.takeIf { it >= 0 } ?: return
        val (upd, cmd) = updater(mutator[index] as S)

        mutator[index] = upd
        commands += cmd
    }

    /**
     * Removes ***ALL*** stack entries that match given [predicate]. You can provide [onPop] callback
     * to produce commands when an entry matching predicate is removed from the stack
     *
     * @param predicate predicate used to filter out entries
     * @param onPop callback which is called when an entry is removed from the stack
     */
    public fun popAll(
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
     * Pushes stack entry and records commands produced by [init] on the top of stack
     *
     * @param init entry initializer
     */
    public fun push(
        init: Update<E, C>
    ) {
        mutator.add(init.first)
        commands += init.second
    }

    /**
     * Pushes stack [entry] on the top of stack
     *
     * @param entry entry to push
     */
    public fun push(
        entry: E
    ) {
        mutator.add(entry)
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
