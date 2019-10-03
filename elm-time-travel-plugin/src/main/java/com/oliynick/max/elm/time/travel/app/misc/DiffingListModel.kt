package com.oliynick.max.elm.time.travel.app.misc

import javax.swing.AbstractListModel

class DiffingListModel<E>(private val diff: DiffCallback<E, E>) : AbstractListModel<E>() {

    private inner class ObservableList<E>(private val delegate: MutableList<E> = mutableListOf()) : MutableList<E> by delegate {

        override fun removeAt(index: Int): E {
            val e = delegate.removeAt(index)

            fireIntervalRemoved(this@DiffingListModel, index, index)
            return e
        }

        override fun set(index: Int, element: E): E {
            val old = delegate[index]

            fireContentsChanged(this@DiffingListModel, index, index)
            return old
        }

        override fun add(index: Int, element: E) {
            delegate.add(index, element)
            fireIntervalAdded(this@DiffingListModel, index, index)
        }
    }

    private val delegate = ObservableList<E>()

    override fun getElementAt(index: Int) = delegate[index]

    override fun getSize(): Int = delegate.size

    fun swap(new: List<E>) {
        delegate.replaceAll(new, diff)
    }

}