package io.github.xlopec.tea.time.travel.plugin.util

import kotlinx.collections.immutable.PersistentMap

internal fun <K, V> PersistentMap<K, V>.nextSelectionForClosingTab(
    closingTabKey: K
): V {
    val currentSelectionIndex = keys.indexOf(closingTabKey)

    require(currentSelectionIndex >= 0) {
        "There is no component $closingTabKey inside debugger instance, available components: $keys"
    }

    return values[calculateNextSelectionIndex(currentSelectionIndex, size)]
}

/**
 * Returns next selection index. Selection rules are the following:
 * * if current tab is in the middle of tab bar, then next left tab is chosen;
 * * if there are no tabs on the left side, then next right tab is chosen.
 * * else 0 is returned (the case when collection consists of a single element)
 *
 * E.g. after we close tab B (selected tab), given tabs a#B#c, we'll have tabs A#c (A tab is selected)
 */
internal fun calculateNextSelectionIndex(
    currentSelectionIndex: Int,
    size: Int,
): Int {
    require(size > 0) { "Can't calculate selection index for empty collection" }
    require(currentSelectionIndex in 0 until size) {
        "Precondition 0 < $currentSelectionIndex (currentSelectionIndex) < $size (size) doesn't hold"
    }
    return (currentSelectionIndex - 1).takeIf { it >= 0 } ?: ((currentSelectionIndex + 1) % size)
}

private operator fun <T> Collection<T>.get(
    i: Int
): T {
    if (this is List<T>) {
        return get(i)
    }

    forEachIndexed { index, t ->
        if (index == i) {
            return t
        }
    }
    error("There is no element for index $i, $this")
}
