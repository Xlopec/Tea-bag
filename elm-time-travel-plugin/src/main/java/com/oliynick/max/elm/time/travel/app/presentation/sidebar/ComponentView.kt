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

class ComponentView(
    private val scope: CoroutineScope,
    private val component: Component<PluginMessage, PluginState>,
    componentState: ComponentDebugState
) : CoroutineScope by scope {

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

        val events = Channel<PluginMessage>()

        snapshotsTree.addMouseListener(object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    snapshotsTree.showActionPopup(e, componentState.id, events)
                }
            }
        })

        launch {
            component(events.consumeAsFlow())
                .mapNotNull { state -> componentState(state, componentState.id) }
                .collect { componentState ->
                    snapshotsModel.swap(componentState.snapshots)
                    stateTreeModel.state = componentState.currentState
                }
        }
    }

}

private fun JTree.showActionPopup(e: MouseEvent, id: ComponentId, events: Channel<PluginMessage>) {
    val row = getClosestRowForLocation(e.x, e.y)

    setSelectionRow(row)

    val menu: JPopupMenu = when (val treeNode = getSubTreeForRow(row)) {
        is SnapshotNode -> snapshotPopup(id, treeNode.snapshot, events)
        is MessageNode -> messagePopup(id, treeNode.message, events)
        is StateNode -> statePopup(id, treeNode.state, events)
        RootNode, is SnapshotTypeNode -> return
    }

    menu.show(e.component, e.x, e.y)
}

private fun JTree.getSubTreeForRow(row: Int): SnapshotTree {
    return (getPathForRow(row).lastPathComponent as DefaultMutableTreeNode).userObject as SnapshotTree
}

private fun componentState(state: PluginState, id: ComponentId) = (state as? Started)?.debugState?.components?.get(id)

private fun snapshotPopup(component: ComponentId, snapshot: Snapshot, events: Channel<PluginMessage>): JPopupMenu {
    return JBPopupMenu("Snapshot ${snapshot.id}").apply {
        add(JBMenuItem("Reset to this", getIcon("updateRunningApplication")).apply {
            addActionListener {
                events.offer(ReApplyState(component, snapshot.state.value))
            }
        })

        add(JBMenuItem("Delete", getIcon("remove")).apply {
            addActionListener {
                events.offer(RemoveSnapshots(component, setOf(snapshot.id)))
            }
        })
    }
}

private fun messagePopup(id: ComponentId, message: RemoteObject, events: Channel<PluginMessage>): JPopupMenu {
    return JBPopupMenu().apply {
        add(JBMenuItem("Apply this message", getIcon("updateRunningApplication")).apply {
            addActionListener {
                events.offer(ReApplyCommands(id, message.value))
            }
        })
    }
}

private fun statePopup(id: ComponentId, state: RemoteObject, events: Channel<PluginMessage>): JPopupMenu {
    return JBPopupMenu().apply {
        add(JBMenuItem("Apply this state", getIcon("updateRunningApplication")).apply {
            addActionListener {
                events.offer(ReApplyState(id, state.value))
            }
        })
    }
}