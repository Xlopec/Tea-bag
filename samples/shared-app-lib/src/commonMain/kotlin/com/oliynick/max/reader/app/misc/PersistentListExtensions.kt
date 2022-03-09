package com.oliynick.max.reader.app.misc

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

inline fun <E, T> Iterable<E>.mapToPersistentList(
    mapper: (E) -> T,
) = with(persistentListOf<T>().builder()) { mapTo(this, mapper) }.build()

inline fun <E, T : Any> Iterable<E>.mapNotNullToPersistentList(
    mapper: (E) -> T?,
) = with(persistentListOf<T>().builder()) { mapNotNullTo(this, mapper) }.build()

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
