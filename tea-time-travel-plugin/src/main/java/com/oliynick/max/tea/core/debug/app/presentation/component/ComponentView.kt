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

package com.oliynick.max.tea.core.debug.app.presentation.component

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.oliynick.max.tea.core.debug.app.domain.cms.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.domain.cms.ReApplyCommands
import com.oliynick.max.tea.core.debug.app.domain.cms.ReApplyState
import com.oliynick.max.tea.core.debug.app.domain.cms.RemoveAllSnapshots
import com.oliynick.max.tea.core.debug.app.domain.cms.RemoveSnapshots
import com.oliynick.max.tea.core.debug.app.domain.cms.Snapshot
import com.oliynick.max.tea.core.debug.app.domain.cms.Started
import com.oliynick.max.tea.core.debug.app.domain.cms.Value
import com.oliynick.max.tea.core.debug.app.presentation.misc.DefaultMouseListener
import com.oliynick.max.tea.core.debug.app.presentation.misc.EntryKeyNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.EntryValueNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.IndexedNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.MessageNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.PropertyNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.RenderTree
import com.oliynick.max.tea.core.debug.app.presentation.misc.RootNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.SnapshotNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.SnapshotTreeModel
import com.oliynick.max.tea.core.debug.app.presentation.misc.SnapshotTreeRenderer
import com.oliynick.max.tea.core.debug.app.presentation.misc.StateNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.StateTreeModel
import com.oliynick.max.tea.core.debug.app.presentation.misc.StateTreeRenderer
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueNode
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.getIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import protocol.ComponentId
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode

class ComponentView(
    private val project: Project,
    private val scope: CoroutineScope,
    private val component: (Flow<PluginMessage>) -> Flow<PluginState>,
    componentState: ComponentDebugState
) : CoroutineScope by scope {

    private lateinit var root: JPanel
    private lateinit var snapshotsTree: JTree
    private lateinit var stateTree: JTree
    private lateinit var searchField: JTextField
    private lateinit var matchCaseCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox

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
                    snapshotsTree.showActionPopup(e, componentState.id, events::offer)
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

private fun JTree.showActionPopup(
    e: MouseEvent,
    id: ComponentId,
    onAction: (PluginMessage) -> Unit
) {
    val row = getClosestRowForLocation(e.x, e.y)

    setSelectionRow(row)

    val menu: JPopupMenu = when (val treeNode = getSubTreeForRow(row)) {
        is SnapshotNode -> snapshotPopup(id, treeNode.snapshot, onAction)
        is MessageNode -> messagePopup(id, treeNode.message, onAction)
        is StateNode -> statePopup(id, treeNode.state, onAction)
        RootNode -> snapshotsPopup(id, onAction)
        is PropertyNode, is ValueNode, is IndexedNode, is EntryKeyNode, is EntryValueNode -> return // todo modify value at this point
    }

    menu.show(e.component, e.x, e.y)
}

private fun JTree.getSubTreeForRow(row: Int): RenderTree {
    return (getPathForRow(row).lastPathComponent as DefaultMutableTreeNode).userObject as RenderTree
}

private fun componentState(
    state: PluginState,
    id: ComponentId
) =
    (state as? Started)?.debugState?.components?.get(id)

private fun snapshotsPopup(
    id: ComponentId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu = JBPopupMenu("Snapshots").apply {
    add(JBMenuItem("Delete all", getIcon("remove")).apply {
        addActionListener {
            onAction(RemoveAllSnapshots(id))
        }
    })
}

private fun snapshotPopup(
    component: ComponentId,
    snapshot: Snapshot,
    onAction: (PluginMessage) -> Unit
): JPopupMenu {
    return JBPopupMenu("Snapshot ${snapshot.id}").apply {
        add(JBMenuItem("Reset to this", getIcon("updateRunningApplication")).apply {
            addActionListener {
                onAction(ReApplyState(component, snapshot.state))
            }
        })

        add(JBMenuItem("Delete", getIcon("remove")).apply {
            addActionListener {
                onAction(RemoveSnapshots(component, setOf(snapshot.id)))
            }
        })
    }
}

private fun messagePopup(
    id: ComponentId,
    message: Value,
    onAction: (PluginMessage) -> Unit
): JPopupMenu {
    return JBPopupMenu().apply {
        add(JBMenuItem("Apply this message", getIcon("updateRunningApplication")).apply {
            addActionListener {
                onAction(ReApplyCommands(id, message))
            }
        })
    }
}

private fun statePopup(
    id: ComponentId,
    state: Value,
    onAction: (PluginMessage) -> Unit
): JPopupMenu {
    return JBPopupMenu().apply {
        add(JBMenuItem("Apply this state", getIcon("updateRunningApplication")).apply {
            addActionListener {
                onAction(ReApplyState(id, state))
            }
        })
    }
}