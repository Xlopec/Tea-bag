package com.oliynick.max.reader.app.navigation

import com.oliynick.max.reader.app.NestedScreen
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.ScreenState
import kotlinx.collections.immutable.PersistentList

typealias NavigationStack = PersistentList<ScreenState>

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

fun NavigationStack.pop(): NavigationStack = if (isEmpty()) this else removeAt(0)

/**
 * This functions moves specified navigation group to the top of the navigation stack and returns
 * new navigation stack
 *
 * r1 - root #1
 *
 * s1,2 - screen #1 the belongs to the root #2
 *
 * s3 - screen #3 that should be drawn fullscreen. Fullscreen objects must be placed on the top,
 * they can't reside in the middle of the stack. You can't navigate from fullscreen to a nested
 * screen since it complicates navigation from user perspective, e.g. consider case when navigating
 * from fullscreen #1 that resides in tab #2 to tab #3 and then navigating back to the tab #1.
 *
 * Screens that relate to the same root are considered to belong to the same stack group.
 * Screens that should be rendered simultaneously are considered to belong to the same drawing group
 * At any moment only one drawing group can be rendered
 *
 * Navigation stack sample:
 *
 * ```
 * s2,2 <- screen to draw
 * s1,2 <- screen to draw
 * r2 <- drawing frame, end of group
 * r1
 * s4,1
 * s5,1
 * ...
 * ```
 *
 * Will produce the following result:
 *
 * ```
 * +-----------------------------+
 * |         Root 2 (r2)         |
 * |   +---------------------+   |
 * |   |   Screen 2 (s2,2)   |   |
 * |   | +-----------------+ |   |
 * |   | | Screen 1 (s1,2) | |   |
 * |   | +-----------------+ |   |
 * |   +---------------------+   |
 * +-----------------------------+
 * ```
 *
 * Navigation stack for screen that occupies whole display:
 *
 * ```
 * s100 <- fullscreen
 * r2
 * s3,2
 * s2,2
 * s1,2
 * r1
 * s4,1
 * s5,1
 * ...
 * ```
 *
 * In this case no drawing frame will be displayed
 *
 * ***Implementation note***. If we ever have to support nesting of the depth more than 1, drawing
 * groups might be implemented as linked list.
 */
fun NavigationStack.floatGroup(
    tabIdx: Int,
    tabId: ScreenId
): NavigationStack {
    require(tabIdx in indices) { "Tab index out of bounds, bounds=${indices}, index=$tabIdx" }

    var bottomGroupIdx = 0

    return foldRightIndexed(this) { idx, screen, acc ->
        // we don't support nesting of the depth more than 1,
        // reference to the tab is enough for now
        if ((screen as? NestedScreen)?.tabId == tabId) {
            bottomGroupIdx++
            acc.swap(idx, 0)
        } else {
            acc
        }
    }.swap(tabIdx, bottomGroupIdx)
}