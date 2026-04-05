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

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

/**
 * Represents a navigation stack, never empty.
 *
 * Clients should never rely on stack ordering as it might change in the future because it's an implementation detail.
 * This means in order to draw the current screen [NavigationStack.screen] property should be used, not [NavigationStack.first] or
 * [NavigationStack.last].
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
 * Creates a new navigation stack.
 *
 * @param R result type of the entry
 * @param T entry type
 * @param top top element of the stack
 * @param other other elements of the stack
 * @return new navigation stack
 */
public fun <R : Any, T : NavStackEntry<R>> stackOf(
    top: T,
    vararg other: T,
): NavigationStack<T> = NavigationStack(persistentListOf(top, *other))

/**
 * Creates a new navigation stack.
 *
 * @param R result type of the entry
 * @param T entry type
 * @param top top element of the stack
 * @param other other elements of the stack
 * @return new navigation stack
 */
public fun <R : Any, T : NavStackEntry<R>> stackOf(
    top: T,
    other: Collection<T>,
): NavigationStack<T> = NavigationStack(persistentListOf(top) + other)

/**
 * Creates a new navigation stack.
 *
 * @param R result type of the entry
 * @param T entry type
 * @param top top element of the stack
 * @return new navigation stack
 */
public fun <R : Any, T : NavStackEntry<R>> stackOf(
    top: T,
): NavigationStack<T> = NavigationStack(persistentListOf(top))

/**
 * Converts a list to a navigation stack or returns `null` if the list is empty.
 *
 * @param R result type of the entry
 * @param T entry type
 * @receiver list of entries
 * @return navigation stack or `null` if the list is empty
 */
public fun <R : Any, T : NavStackEntry<R>> List<T>.toStackOrNull(): NavigationStack<T>? = firstOrNull()?.let { stackOf(it, drop(1)) }
