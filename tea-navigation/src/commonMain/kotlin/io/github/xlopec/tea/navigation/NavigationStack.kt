package io.github.xlopec.tea.navigation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

/**
 * Represents navigation stack, never empty.
 *
 * Clients should never rely on stack ordering as it might change in the future because it's an implementation details.
 * This means in order to draw current screen [NavigationStack.screen] property should be used, not [NavigationStack.first] or
 * [NavigationStack.last]
 */
@Immutable
// TODO make it a value class
public class NavigationStack<out T> internal constructor(
    internal val value: PersistentList<T>,
) : ImmutableList<T> by value {
    init {
        require(value.isNotEmpty()) { "Navigation stack cannot be empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NavigationStack<*>

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = value.toString()
}

/**
 * Creates a new navigation stack
 */
public fun <R, T : NavStackEntry<R>> stackOf(
    top: T,
    vararg other: T,
): NavigationStack<T> = NavigationStack(persistentListOf(top, *other))

/**
 * Creates a new navigation stack
 */
public fun <R, T : NavStackEntry<R>> stackOf(
    top: T,
    other: Collection<T>,
): NavigationStack<T> = NavigationStack(persistentListOf(top) + other)

/**
 * Creates a new navigation stack
 */
public fun <R, T : NavStackEntry<R>> stackOf(
    top: T,
): NavigationStack<T> = NavigationStack(persistentListOf(top))

/**
 * Converts list to navigation stack or returns null if the list is null
 */
public fun <R, T : NavStackEntry<R>> List<T>.toStackOrNull(): NavigationStack<T>? = firstOrNull()?.let { stackOf(it, drop(1)) }
