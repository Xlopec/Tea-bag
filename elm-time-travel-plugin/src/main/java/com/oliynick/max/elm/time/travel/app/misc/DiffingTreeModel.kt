package com.oliynick.max.elm.time.travel.app.misc

import com.oliynick.max.elm.time.travel.app.Instance
import com.oliynick.max.elm.time.travel.app.LevelNode
import com.oliynick.max.elm.time.travel.app.toJTree
import javax.swing.tree.*

class DiffingTreeModel private constructor(private val delegate: DefaultTreeModel,
                                           initial: List<Instance>) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<Instance> = emptyList()): DiffingTreeModel {
            return DiffingTreeModel(DefaultTreeModel(DefaultMutableTreeNode("Root", true)), data)
        }
    }

    private val diff = object : DiffCallback<Instance, Instance> {
        override fun areItemsTheSame(oldItem: Instance, newItem: Instance): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Instance, newItem: Instance): Boolean = oldItem === newItem
    }

    private val updateCallback = object : UpdateCallback<Instance, Instance> {

        override fun onContentUpdated(oldItem: Instance, oldIndex: Int, newItem: Instance, newIndex: Int) {
            delegate.nodeChanged((rootNode.getChildAt(oldIndex) as MutableTreeNode)
                .also { node -> node.setUserObject(newItem) })
        }

        override fun onItemInserted(item: Instance, index: Int) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }
            delegate.insertNodeInto(item.toJTree(), rootNode, index)
        }

        override fun onItemRemoved(item: Instance, index: Int) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = initial.toMutableList()

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<Instance>) {
        logicalNodes.replaceAll(new, diff, updateCallback)
    }

}