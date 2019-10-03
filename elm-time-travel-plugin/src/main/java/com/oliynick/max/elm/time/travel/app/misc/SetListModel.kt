package com.oliynick.max.elm.time.travel.app.misc

import org.apache.commons.collections.set.ListOrderedSet
import javax.swing.AbstractListModel

class SetListModel<E> : AbstractListModel<E>() {

    private val elements = ListOrderedSet.decorate(mutableListOf<E>())

    @Suppress("UNCHECKED_CAST")
    override fun getElementAt(index: Int) = elements.get(index) as E

    override fun getSize(): Int = elements.size

    operator fun plusAssign(element: E) {
        val index = size

        if (elements.add(element)) {
            fireIntervalAdded(this, index, index)
        }
    }

    operator fun plusAssign(elements: Iterable<E>) = elements.forEach { e -> this += e }

    operator fun minusAssign(element: E) {
        val i = elements.indexOf(element)

        if (i >= 0) {
            elements.remove(i)
            fireIntervalRemoved(this, i, i)
        }
    }

    operator fun minusAssign(elements: Iterable<E>) = elements.forEach { e -> this -= e }

}