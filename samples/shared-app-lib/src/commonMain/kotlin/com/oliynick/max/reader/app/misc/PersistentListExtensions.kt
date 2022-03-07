package com.oliynick.max.reader.app.misc

import kotlinx.collections.immutable.PersistentList

inline fun <E> PersistentList<E>.replace(
    e: E,
    predicate: (E) -> Boolean,
): PersistentList<E> {
    val i = indexOfFirst(predicate)

    return if (i >= 0) {
        set(i, e)
    } else {
        this
    }
}

inline fun <E> PersistentList<E>.remove(
    predicate: (E) -> Boolean,
): PersistentList<E> {
    val i = indexOfFirst(predicate)

    return if (i >= 0) {
        removeAt(i)
    } else {
        this
    }
}
