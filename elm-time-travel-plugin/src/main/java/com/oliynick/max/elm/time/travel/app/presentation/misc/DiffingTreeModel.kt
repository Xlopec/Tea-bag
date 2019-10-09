package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.RemoteCommand
import com.oliynick.max.elm.time.travel.app.misc.DiffCallback
import com.oliynick.max.elm.time.travel.app.misc.UpdateCallback
import com.oliynick.max.elm.time.travel.app.misc.replaceAll
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class DiffingTreeModel private constructor(private val delegate: DefaultTreeModel,
                                           initial: List<RemoteCommand>) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<RemoteCommand> = emptyList()): DiffingTreeModel {
            return DiffingTreeModel(DefaultTreeModel(DefaultMutableTreeNode("Root", true)), data)
        }
    }

    private val diff = object : DiffCallback<RemoteCommand, RemoteCommand> {
        override fun areItemsTheSame(oldItem: RemoteCommand, newItem: RemoteCommand): Boolean = oldItem === newItem
        override fun areContentsTheSame(oldItem: RemoteCommand, newItem: RemoteCommand): Boolean = oldItem === newItem
    }

    private val updateCallback = object : UpdateCallback<RemoteCommand, RemoteCommand> {

        override fun onContentUpdated(oldItem: RemoteCommand, oldIndex: Int, newItem: RemoteCommand, newIndex: Int) {
            rootNode.remove(oldIndex)
            delegate.insertNodeInto(newItem.representation.toJTree(), rootNode, oldIndex)
        }

        override fun onItemInserted(item: RemoteCommand, index: Int) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }
            delegate.insertNodeInto(item.representation.toJTree(), rootNode, index)
        }

        override fun onItemRemoved(item: RemoteCommand, index: Int) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = mutableListOf<RemoteCommand>().replaceAll(initial, diff, updateCallback)

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<RemoteCommand>) {
        logicalNodes.replaceAll(new, diff, updateCallback)
    }

    operator fun get(i: Int) = logicalNodes[i]

}