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