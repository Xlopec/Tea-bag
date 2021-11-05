package com.oliynick.max.reader.app

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import kotlinx.collections.immutable.PersistentList

typealias NavigationStack = PersistentList<ScreenState>

interface TabScreen : ScreenState {
    // things for consideration:
    // 1 how to make fast search & update for nested screens
    // 2 how to keep class layout as simple as possible with p1 in mind
    // 3 how to avoid code duplication? (consider Arrow Meta optics API)
    // fixme this probably should go as extensions
    // probably I can make typealias for this and implement all the necessary operations on the top of it
    val screens: NavigationStack
    fun pop(): TabScreen
    fun <T : ScreenState> update(
        id: ScreenId,
        how: (T) -> UpdateWith<T, Command>
    ): UpdateWith<TabScreen, Command>
}

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

fun NavigationStack.swap(
    i: Int,
    j: Int,
): NavigationStack {

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