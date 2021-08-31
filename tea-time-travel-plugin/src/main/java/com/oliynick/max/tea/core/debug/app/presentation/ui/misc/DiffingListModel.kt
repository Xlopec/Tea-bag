/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import com.oliynick.max.tea.core.debug.app.misc.DiffCallback
import com.oliynick.max.tea.core.debug.app.misc.replaceAll
import javax.swing.AbstractListModel

class DiffingListModel<E>(private val diff: DiffCallback<E, E>) : AbstractListModel<E>() {

    private inner class ObservableList<E>(private val delegate: MutableList<E> = mutableListOf()) : MutableList<E> by delegate {

        override fun removeAt(index: Int): E {
            val e = delegate.removeAt(index)

            fireIntervalRemoved(this@DiffingListModel, index, index)
            return e
        }

        override fun set(
            index: Int,
            element: E
        ): E {
            val old = delegate[index]

            fireContentsChanged(this@DiffingListModel, index, index)
            return old
        }

        override fun add(
            index: Int,
            element: E
        ) {
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
