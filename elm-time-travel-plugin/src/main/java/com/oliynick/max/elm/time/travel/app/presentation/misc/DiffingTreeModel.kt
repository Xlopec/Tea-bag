package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.domain.TypeNode
import com.oliynick.max.elm.time.travel.app.misc.DiffCallback
import com.oliynick.max.elm.time.travel.app.misc.UpdateCallback
import com.oliynick.max.elm.time.travel.app.misc.replaceAll
import javax.swing.tree.*

class DiffingTreeModel private constructor(private val delegate: DefaultTreeModel,
                                           initial: List<TypeNode>) : TreeModel by delegate {

    companion object {
        fun newInstance(data: List<TypeNode> = emptyList()): DiffingTreeModel {
            return DiffingTreeModel(DefaultTreeModel(DefaultMutableTreeNode("Root", true)), data)
        }
    }

    private val diff = object : DiffCallback<TypeNode, TypeNode> {
        override fun areItemsTheSame(oldItem: TypeNode, newItem: TypeNode): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: TypeNode, newItem: TypeNode): Boolean = oldItem === newItem
    }

    private val updateCallback = object : UpdateCallback<TypeNode, TypeNode> {

        override fun onContentUpdated(oldItem: TypeNode, oldIndex: Int, newItem: TypeNode, newIndex: Int) {
            delegate.nodeChanged((rootNode.getChildAt(oldIndex) as MutableTreeNode)
                .also { node -> node.setUserObject(newItem) })
        }

        override fun onItemInserted(item: TypeNode, index: Int) {
            require(index == rootNode.childCount) { "$index != ${rootNode.childCount}" }
            delegate.insertNodeInto(item.toJTree(), rootNode, index)
        }

        override fun onItemRemoved(item: TypeNode, index: Int) {
            delegate.removeNodeFromParent(rootNode.getChildAt(index) as MutableTreeNode)
        }
    }

    private val logicalNodes = initial.toMutableList()

    private val rootNode: MutableTreeNode
        inline get() = delegate.root as MutableTreeNode

    fun swap(new: List<TypeNode>) {
        logicalNodes.replaceAll(new, diff, updateCallback)
    }

}