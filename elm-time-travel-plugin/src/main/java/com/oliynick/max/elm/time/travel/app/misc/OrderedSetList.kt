package com.oliynick.max.elm.time.travel.app.misc

import org.apache.commons.collections.set.ListOrderedSet

class OrderedSetList<E> {

    private val elements = ListOrderedSet.decorate(mutableListOf<E>())

    val size: Int
        get() = elements.size

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int) = elements.get(index) as E

    operator fun plusAssign(element: E) {
        elements.add(element)
    }

    operator fun plusAssign(elements: Iterable<E>) = elements.forEach { e -> this += e }

    operator fun minusAssign(element: E) {
        elements.remove(element)
    }

    operator fun minusAssign(elements: Iterable<E>) = elements.forEach { e -> this -= e }

}

val OrderedSetList<*>.isEmpty get() = size == 0

val OrderedSetList<*>.isNotEmpty get() = !isEmpty