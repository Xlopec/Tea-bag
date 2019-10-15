/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.Snapshot
import com.oliynick.max.elm.time.travel.app.misc.UpdateCallback
import com.oliynick.max.elm.time.travel.app.misc.replaceAll
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class DiffingTreeModel private constructor(private val delegate: DefaultTreeModel,
                                           initial: List<Snapshot>) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<Snapshot> = emptyList()): DiffingTreeModel {
            return DiffingTreeModel(DefaultTreeModel(DefaultMutableTreeNode("Root", true)), data)
        }
    }

    private val updateCallback = object : UpdateCallback<Snapshot, Snapshot> {

        override fun onContentUpdated(oldItem: Snapshot, oldIndex: Int, newItem: Snapshot, newIndex: Int) {
            rootNode.remove(oldIndex)
            delegate.insertNodeInto(newItem.toComponentSubTree(), rootNode, oldIndex)
        }

        override fun onItemInserted(item: Snapshot, index: Int) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }

            delegate.insertNodeInto(item.toComponentSubTree(), rootNode, index)
        }

        override fun onItemRemoved(item: Snapshot, index: Int) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = mutableListOf<Snapshot>().replaceAll(initial, RefDiffer, updateCallback)

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<Snapshot>) {
        logicalNodes.replaceAll(new, RefDiffer, updateCallback)
    }

    operator fun get(i: Int) = logicalNodes[i]

}

private fun Snapshot.toComponentSubTree(): DefaultMutableTreeNode {
    return DefaultMutableTreeNode(this)
        .apply {
            add(DefaultMutableTreeNode("Message").apply {
                add(message.representation.toJTree())
            })
            add(DefaultMutableTreeNode("State").apply {
                add(state.representation.toJTree())
            })
        }
}