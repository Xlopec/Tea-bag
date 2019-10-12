package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.misc.DiffingTreeModel
import com.oliynick.max.elm.time.travel.app.presentation.misc.ObjectTreeRenderer
import com.oliynick.max.elm.time.travel.app.presentation.misc.setOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

class ComponentView(
    private val scope: CoroutineScope,
    private val component: Component<PluginMessage, PluginState>,
    componentState: ComponentDebugState
) : CoroutineScope by scope {

    private lateinit var root: JPanel
    private lateinit var commandsTree: JTree
    private lateinit var statesTree: JTree
    private lateinit var applyCommandButton: JLabel
    private lateinit var removeCommandButton: JLabel

    val _root get() = root

    init {
        commandsTree.cellRenderer = ObjectTreeRenderer("Commands")
        statesTree.cellRenderer = ObjectTreeRenderer("States")

        val commandsTreeModel = DiffingTreeModel.newInstance(componentState.commands)
        val statesTreeModel = DiffingTreeModel.newInstance(componentState.states)

        commandsTree.model = commandsTreeModel
        statesTree.model = statesTreeModel

        val componentEvents = Channel<PluginMessage>()

        launch {
            component(componentEvents.consumeAsFlow())
                .mapNotNull { (it as? Started)?.debugState?.components?.get(componentState.id) }
                .collect { componentState ->
                    commandsTreeModel.swap(componentState.commands)
                    statesTreeModel.swap(componentState.states)
                }
        }

        commandsTree.selectionModel.addTreeSelectionListener {
            removeCommandButton.isEnabled = commandsTree.getSelectedCommandIndex() != -1
            applyCommandButton.isEnabled = removeCommandButton.isEnabled
        }

        applyCommandButton.setOnClickListener {
            val i = commandsTree.getSelectedCommandIndex()

            if (i >= 0) {
                componentEvents.offer(ReApplyCommands(componentState.id, listOf(commandsTreeModel[i].value)))
            }
        }

        removeCommandButton.setOnClickListener {
            val i = commandsTree.getSelectedCommandIndex()

            if (i >= 0) {
                componentEvents.offer(RemoveCommands(componentState.id, intArrayOf(i)))
            }
        }
    }

}

private fun JTree.getSelectedCommandIndex(): Int {
    val selected = selectionModel.selectionPath?.lastPathComponent as? MutableTreeNode ?: return -1

    return (model.root as MutableTreeNode)
        .children()
        .asSequence()
        .mapIndexed { index, treeNode -> index to treeNode }
        .find { (_, node) -> node.contains(selected) }?.first ?: -1
}

private fun TreeNode.contains(node: TreeNode): Boolean {
    return this == node || children().asSequence().any { ch -> ch.contains(node) }
}