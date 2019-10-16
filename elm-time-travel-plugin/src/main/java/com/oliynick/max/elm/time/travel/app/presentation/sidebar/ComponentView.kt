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

package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.misc.*
import com.oliynick.max.elm.time.travel.protocol.ComponentId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode

class ComponentView(private val scope: CoroutineScope,
                    private val component: Component<PluginMessage, PluginState>,
                    componentState: ComponentDebugState) : CoroutineScope by scope {

    private lateinit var root: JPanel
    private lateinit var snapshotsTree: JTree
    private lateinit var stateTree: JTree
    private lateinit var applyCommandButton: JLabel
    private lateinit var removeCommandButton: JLabel

    val _root get() = root

    init {
        val snapshotsModel = SnapshotTreeModel.newInstance(componentState.snapshots)

        snapshotsTree.model = snapshotsModel
        snapshotsTree.cellRenderer = SnapshotTreeRenderer

        val stateTreeModel = StateTreeModel.newInstance(componentState.currentState)

        stateTree.model = stateTreeModel
        stateTree.cellRenderer = StateTreeRenderer

        val componentEvents = Channel<PluginMessage>()

        launch {
            component(componentEvents.consumeAsFlow())
                .mapNotNull { state -> componentState(state, componentState.id) }
                .collect { componentState ->
                    snapshotsModel.swap(componentState.snapshots)
                    stateTreeModel.state = componentState.currentState
                }
        }

        snapshotsTree.selectionModel.addTreeSelectionListener {
            removeCommandButton.isEnabled = snapshotsTree.getSelectedCommandIndex() != -1
            applyCommandButton.isEnabled = removeCommandButton.isEnabled
        }

        snapshotsTree.addMouseListener(object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val row = snapshotsTree.getClosestRowForLocation(e.x, e.y)

                    snapshotsTree.setSelectionRow(row)
                    (snapshotsTree.getPathForRow(row).lastPathComponent as DefaultMutableTreeNode).also {

                        when (val payload = it.userObject) {
                            is Snapshot -> buildPopup(componentState.id, payload, componentEvents).show(e.component, e.x, e.y)
                            "Message" -> buildMessagePopup(componentEvents).show(e.component, e.x, e.y)
                            "State" -> buildStatePopup(componentEvents).show(e.component, e.x, e.y)
                        }
                    }
                }
            }
        })

        applyCommandButton.setOnClickListener {
            val i = snapshotsTree.getSelectedCommandIndex()

            if (i >= 0) {
                //componentEvents.offer(ReApplyCommands(componentState.id, listOf(commandsTreeModel[i].value)))
            }
        }

        removeCommandButton.setOnClickListener {
            val i = snapshotsTree.getSelectedCommandIndex()

            if (i >= 0) {
                //componentEvents.offer(RemoveSnapshots(componentState.id, intArrayOf(i)))
            }
        }
    }

}

private fun componentState(state: PluginState, id: ComponentId) = (state as? Started)?.debugState?.components?.get(id)

private fun stateTreeModel(state: RemoteObject): DefaultTreeModel {
    return DefaultTreeModel(DefaultMutableTreeNode("State").also { it.add(state.representation.toJTree()) })
}

private fun buildPopup(component: ComponentId, snapshot: Snapshot, events: Channel<PluginMessage>): JPopupMenu {
    val menu = JBPopupMenu("Snapshot ${snapshot.id}")

    menu.add(JBMenuItem("Reset to this", getIcon("updateRunningApplication")).apply {
        addActionListener {
            events.offer(ReApplyState(component, snapshot.state.value))
        }
    })

    menu.add(JBMenuItem("Delete", getIcon("remove")).apply {
        addActionListener {
            events.offer(RemoveSnapshots(component, setOf(snapshot.id)))
        }
    })

    return menu
}

private fun buildMessagePopup(events: Channel<PluginMessage>): JPopupMenu {
    val menu = JBPopupMenu()

    menu.add(JBMenuItem("Apply this message", getIcon("updateRunningApplication")).apply {
        addMouseListener(object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                //events.offer()
            }
        })
    })

    return menu
}

private fun buildStatePopup(events: Channel<PluginMessage>): JPopupMenu {
    val menu = JBPopupMenu()

    menu.add(JBMenuItem("Apply this state", getIcon("updateRunningApplication")).apply {
        addMouseListener(object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {

            }
        })
    })

    return menu
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