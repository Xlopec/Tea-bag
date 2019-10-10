package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.RemoteObject
import com.oliynick.max.elm.time.travel.app.misc.UpdateCallback
import com.oliynick.max.elm.time.travel.app.misc.replaceAll
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class DiffingTreeModel private constructor(private val delegate: DefaultTreeModel,
                                           initial: List<RemoteObject>) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<RemoteObject> = emptyList()): DiffingTreeModel {
            return DiffingTreeModel(DefaultTreeModel(DefaultMutableTreeNode("Root", true)), data)
        }
    }

    private val updateCallback = object : UpdateCallback<RemoteObject, RemoteObject> {

        override fun onContentUpdated(oldItem: RemoteObject, oldIndex: Int, newItem: RemoteObject, newIndex: Int) {
            rootNode.remove(oldIndex)
            delegate.insertNodeInto(newItem.representation.toJTree(), rootNode, oldIndex)
        }

        override fun onItemInserted(item: RemoteObject, index: Int) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }
            delegate.insertNodeInto(item.representation.toJTree(), rootNode, index)
        }

        override fun onItemRemoved(item: RemoteObject, index: Int) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = mutableListOf<RemoteObject>().replaceAll(initial, RefDiffer, updateCallback)

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<RemoteObject>) {
        logicalNodes.replaceAll(new, RefDiffer, updateCallback)
    }

    operator fun get(i: Int) = logicalNodes[i]

}