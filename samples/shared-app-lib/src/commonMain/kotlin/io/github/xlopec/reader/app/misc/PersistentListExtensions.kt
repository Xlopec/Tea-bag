/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app.misc

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

internal inline fun <E, T> Iterable<E>.mapToPersistentList(
    mapper: (E) -> T,
) = with(persistentListOf<T>().builder()) { mapTo(this, mapper) }.build()

internal inline fun <E, T : Any> Iterable<E>.mapNotNullToPersistentList(
    mapper: (E) -> T?,
) = with(persistentListOf<T>().builder()) { mapNotNullTo(this, mapper) }.build()

internal inline fun <E> PersistentList<E>.replace(
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

internal inline fun <E> PersistentList<E>.remove(
    predicate: (E) -> Boolean,
): PersistentList<E> {
    val i = indexOfFirst(predicate)

    return if (i >= 0) {
        removeAt(i)
    } else {
        this
    }
}
