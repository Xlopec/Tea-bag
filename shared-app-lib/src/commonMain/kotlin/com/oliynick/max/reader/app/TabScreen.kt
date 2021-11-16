package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import kotlinx.collections.immutable.PersistentList

typealias NavigationStack = PersistentList<ScreenState>

inline fun NavigationStack.update(
    id: ScreenId,
    how: (ScreenState) -> UpdateWith<ScreenState, Command>
): UpdateWith<NavigationStack, Command> {
    val i = indexOfFirst { it.id == id }

    if (i < 0) {
        error("unknown screen for id=$id, screens=$this")
    }

    val (victim, commands) = how(this[i])

    return set(i, victim) command commands
}

fun <T> PersistentList<T>.swap(
    i: Int,
    j: Int,
): PersistentList<T> {

    if (i == j) return this

    val tmp = this[j]

    return set(j, this[i]).set(i, tmp)
}

fun NavigationStack.push(
    s: ScreenState
) = add(0, s)

inline val NavigationStack.screen: ScreenState get() = this[0]

operator fun NavigationStack.contains(
    id: ScreenId
) = find { it.id == id } != null

fun NavigationStack.pop(): NavigationStack = if (isEmpty()) this else removeAt(0)