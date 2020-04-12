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

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.presentation.component

import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.oliynick.max.tea.core.debug.app.component.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.component.cms.ReApplyMessage
import com.oliynick.max.tea.core.debug.app.component.cms.ReApplyState
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveAllSnapshots
import com.oliynick.max.tea.core.debug.app.component.cms.RemoveSnapshots
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateFilter
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.Filter
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.FilteredSnapshot
import com.oliynick.max.tea.core.debug.app.domain.Invalid
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.REMOVE_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.UPDATE_RUNNING_APP_ICON
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
import com.oliynick.max.tea.core.debug.app.presentation.misc.mergeWith
import com.oliynick.max.tea.core.debug.app.presentation.misc.selections
import com.oliynick.max.tea.core.debug.app.presentation.misc.textChanges
import com.oliynick.max.tea.core.debug.app.presentation.misc.textSafe
import com.oliynick.max.tea.core.debug.app.presentation.misc.toReadableString
import com.oliynick.max.tea.core.debug.app.presentation.sidebar.exceptionBalloonFillColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import protocol.ComponentId
import java.awt.Color
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeSelectionModel

private const val INPUT_TIMEOUT_MILLIS = 400L

class ComponentView private constructor(
    initial: ComponentDebugState
) {

    companion object {

        private val ERROR_COLOR = Color(exceptionBalloonFillColor.rgb)

        fun new(
            scope: CoroutineScope,
            component: (Flow<PluginMessage>) -> Flow<ComponentDebugState>,
            componentState: ComponentDebugState
        ): JPanel =
            ComponentView(componentState)
                .apply { scope.launch { render(componentState.id, componentState.filter, component) } }.root

        private fun ComponentView.filterUpdates(
            id: ComponentId,
            filter: Filter
        ): Flow<UpdateFilter> {

            val regexFlow = regexCheckBox.asRegexFlow(filter)
            val wordsFlow = wordsCheckBox.asWordsFlow(filter)
            val substringFlow = substringFlow(regexFlow, wordsFlow)
            val optionsChanges = merge(regexFlow, wordsFlow, substringFlow)
                .distinctUntilChanged()
                .filterNotNull()

            return filterUpdates(id, searchField.textFlow(), matchCaseCheckBox.asMatchCaseFlow(filter), optionsChanges)
        }

        private suspend fun ComponentView.render(
            id: ComponentId,
            filter: Filter,
            component: (Flow<PluginMessage>) -> Flow<ComponentDebugState>
        ) = component(filterUpdates(id, filter).mergeWith(snapshotsTree.asOptionMenuUpdates(id)))
            .collect { componentState -> render(componentState) }

    }

    private lateinit var root: JPanel
    private lateinit var snapshotsTree: JTree
    private lateinit var stateTree: JTree
    private lateinit var searchField: JTextField
    private lateinit var matchCaseCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox
    private lateinit var wordsCheckBox: JCheckBox

    private val snapshotsModel = SnapshotTreeModel.newInstance(initial.filteredSnapshots)
    private val stateTreeModel = StateTreeModel.newInstance(initial.state)

    init {
        snapshotsTree.model = snapshotsModel
        snapshotsTree.transferHandler = TreeRowValueTransferHandler
        snapshotsTree.selectionModel.selectionMode = TreeSelectionModel.CONTIGUOUS_TREE_SELECTION
        snapshotsTree.cellRenderer = SnapshotTreeRenderer

        stateTree.model = stateTreeModel
        stateTree.cellRenderer = StateTreeRenderer
    }

    private fun render(
        state: ComponentDebugState
    ) {
        snapshotsModel.swap(state.filteredSnapshots)
        stateTreeModel.state = state.state

        val validatedPredicate = state.filter.predicate

        if (validatedPredicate == null || validatedPredicate.isValid()) {
            searchField.background = null
            searchField.toolTipText = null
        } else {
            searchField.background = ERROR_COLOR
            searchField.toolTipText = (validatedPredicate as Invalid).message
        }

        updateFilterOptions(state.filter)

        searchField.textSafe = validatedPredicate?.input ?: ""
    }

    private fun updateFilterOptions(
        filter: Filter
    ) {
        matchCaseCheckBox.isSelected = !filter.ignoreCase
        regexCheckBox.isSelected = filter.option === FilterOption.REGEX
        wordsCheckBox.isSelected = filter.option === FilterOption.WORDS
    }

}

private fun filterUpdates(
    id: ComponentId,
    textChanges: Flow<String>,
    ignoreCaseChanges: Flow<Boolean>,
    optionsChanges: Flow<FilterOption>
) = combine(textChanges, ignoreCaseChanges, optionsChanges) { text, ignoreCase, option -> UpdateFilter(id, text, ignoreCase, option) }

private fun substringFlow(
    regexFlow: Flow<FilterOption?>,
    wordsFlow: Flow<FilterOption?>
) = regexFlow.combine(wordsFlow) { reg, word -> if (reg === word && reg == null) FilterOption.SUBSTRING else null }

private fun JTree.asOptionMenuUpdates(
    id: ComponentId
): Flow<PluginMessage> =
    callbackFlow {

        val l = object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showActionPopup(e, id) { offer(it) }
                }
            }
        }

        addMouseListener(l)

        awaitClose { removeMouseListener(l) }
    }

private fun JTextField.textFlow() =
    textChanges()
        .onStart { emit("") }
        .debounce(INPUT_TIMEOUT_MILLIS)

private fun JCheckBox.asWordsFlow(
    filter: Filter
) =
    selections()
        .onStart { emit(filter.option === FilterOption.WORDS) }
        .map { isChecked -> if (isChecked) FilterOption.WORDS else null }

private fun JCheckBox.asRegexFlow(
    filter: Filter
) =
    selections()
        .onStart { emit(filter.option === FilterOption.REGEX) }
        .map { isChecked -> if (isChecked) FilterOption.REGEX else null }

private fun JCheckBox.asMatchCaseFlow(
    filter: Filter
) =
    selections()
        .onStart { emit(!filter.ignoreCase) }
        .map { isSelected -> !isSelected }

private fun JTree.showActionPopup(
    e: MouseEvent,
    id: ComponentId,
    onAction: (PluginMessage) -> Unit
) {
    val row = getClosestRowForLocation(e.x, e.y)

    setSelectionRow(row)

    val menu: JPopupMenu = when (val treeNode = getSubTreeForRow(row)) {
        is SnapshotNode -> SnapshotPopup(id, treeNode.snapshot, onAction)
        is MessageNode -> MessagePopup(id, treeNode.id, onAction)
        is StateNode -> StatePopup(id, treeNode.id, onAction)
        RootNode -> SnapshotsPopup(id, onAction)
        is PropertyNode, is ValueNode, is IndexedNode, is EntryKeyNode, is EntryValueNode -> return // todo modify value at this point
    }

    menu.show(e.component, e.x, e.y)
}

private fun JTree.getSubTreeForRow(row: Int): RenderTree {
    return (getPathForRow(row).lastPathComponent as DefaultMutableTreeNode).userObject as RenderTree
}

private fun SnapshotsPopup(
    id: ComponentId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu = JBPopupMenu("Snapshots").apply {
    add(JBMenuItem("Delete all", REMOVE_ICON).apply {
        addActionListener {
            onAction(RemoveAllSnapshots(id))
        }
    })
}

private fun SnapshotPopup(
    component: ComponentId,
    snapshot: FilteredSnapshot,
    onAction: (PluginMessage) -> Unit
): JPopupMenu =
    JBPopupMenu("Snapshot ${snapshot.meta.id.value}").apply {
        add(JBMenuItem(
            "Reset to this",
            UPDATE_RUNNING_APP_ICON
        ).apply {
            addActionListener {
                onAction(ReApplyState(component, snapshot.meta.id))
            }
        })

        add(JBMenuItem("Delete", REMOVE_ICON).apply {
            addActionListener {
                onAction(RemoveSnapshots(component, snapshot.meta.id))
            }
        })
    }

private fun MessagePopup(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu =
    JBPopupMenu().apply {
        add(JBMenuItem(
            "Apply this message", UPDATE_RUNNING_APP_ICON
        ).apply {
            addActionListener {
                onAction(ReApplyMessage(componentId, snapshotId))
            }
        })
    }

private fun StatePopup(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu =
    JBPopupMenu().apply {
        add(JBMenuItem("Apply this state", UPDATE_RUNNING_APP_ICON).apply {
            addActionListener {
                onAction(ReApplyState(componentId, snapshotId))
            }
        })
    }

private object TreeRowValueTransferHandler : TransferHandler() {
    override fun getSourceActions(c: JComponent?): Int = COPY
    override fun createTransferable(c: JComponent): Transferable? {

        val tree = c as JTree

        return tree.selectionRows
            ?.map(tree::getSubTreeForRow)
            ?.joinToString { r -> r.toReadableString(tree.model) }
            ?.let(::StringSelection)
    }
}