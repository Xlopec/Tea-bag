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

package com.oliynick.max.tea.core.debug.app.presentation.component

import com.oliynick.max.tea.core.debug.app.domain.FilteredSnapshot
import com.oliynick.max.tea.core.debug.app.misc.UpdateCallback
import com.oliynick.max.tea.core.debug.app.misc.replaceAll
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.*
import javax.swing.tree.*

class SnapshotTreeModel private constructor(
    private val delegate: DefaultTreeModel,
    initial: List<FilteredSnapshot>
) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<FilteredSnapshot> = emptyList()): SnapshotTreeModel {
            return SnapshotTreeModel(DefaultTreeModel(DefaultMutableTreeNode(RootNode, true)), data)
        }
    }

    private val updateCallback = object : UpdateCallback<FilteredSnapshot, FilteredSnapshot> {

        override fun onContentUpdated(
            oldItem: FilteredSnapshot,
            oldIndex: Int,
            newItem: FilteredSnapshot,
            newIndex: Int
        ) {
            //fixme mutate instead
            rootNode.remove(oldIndex)
            delegate.insertNodeInto(newItem.toComponentSubTree(), rootNode, oldIndex)
        }

        override fun onItemInserted(
            item: FilteredSnapshot,
            index: Int
        ) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }

            delegate.insertNodeInto(item.toComponentSubTree(), rootNode, index)
        }

        override fun onItemRemoved(
            item: FilteredSnapshot,
            index: Int
        ) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = mutableListOf<FilteredSnapshot>()
        .replaceAll(initial, EqDiffer, updateCallback)

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<FilteredSnapshot>) {
        logicalNodes.replaceAll(new, EqDiffer, updateCallback)
    }

    override fun getRoot() = rootNode

    operator fun get(i: Int) = logicalNodes[i]

}

private fun FilteredSnapshot.toComponentSubTree(): DefaultMutableTreeNode =
    DefaultMutableTreeNode(SnapshotNode(this))
        .apply {

            if (message != null) {
                add(DefaultMutableTreeNode(MessageNode(meta.id, message)).apply {
                    add(message.toJTree())
                })
            }

            if (state != null) {
                add(DefaultMutableTreeNode(StateNode(meta.id, state)).apply {
                    add(state.toJTree())
                })
            }
        }