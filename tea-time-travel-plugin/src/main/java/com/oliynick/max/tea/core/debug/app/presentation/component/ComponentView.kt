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
import com.oliynick.max.tea.core.debug.app.component.cms.Started
import com.oliynick.max.tea.core.debug.app.component.cms.UpdateFilter
import com.oliynick.max.tea.core.debug.app.component.cms.component
import com.oliynick.max.tea.core.debug.app.domain.ComponentDebugState
import com.oliynick.max.tea.core.debug.app.domain.Filter
import com.oliynick.max.tea.core.debug.app.domain.FilterOption
import com.oliynick.max.tea.core.debug.app.domain.FilteredSnapshot
import com.oliynick.max.tea.core.debug.app.domain.Invalid
import com.oliynick.max.tea.core.debug.app.domain.Predicate
import com.oliynick.max.tea.core.debug.app.domain.Settings
import com.oliynick.max.tea.core.debug.app.domain.SnapshotId
import com.oliynick.max.tea.core.debug.app.domain.Validated
import com.oliynick.max.tea.core.debug.app.domain.isValid
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.REMOVE_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.ActionIcons.UPDATE_RUNNING_APP_ICON
import com.oliynick.max.tea.core.debug.app.presentation.misc.DefaultMouseListener
import com.oliynick.max.tea.core.debug.app.presentation.misc.EntryKeyNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.EntryValueNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.IndexedNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.MessageNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.PropertyNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.RootNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.SnapshotNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.StateNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.TreeRowValueTransferHandler
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueFormatter
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueNode
import com.oliynick.max.tea.core.debug.app.presentation.misc.getSubTreeForRow
import com.oliynick.max.tea.core.debug.app.presentation.misc.mergeWith
import com.oliynick.max.tea.core.debug.app.presentation.misc.selections
import com.oliynick.max.tea.core.debug.app.presentation.misc.textChanges
import com.oliynick.max.tea.core.debug.app.presentation.misc.textSafe
import com.oliynick.max.tea.core.debug.app.presentation.misc.toReadableStringDetailed
import com.oliynick.max.tea.core.debug.app.presentation.misc.toReadableStringShort
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
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.TreeSelectionModel

private const val INPUT_TIMEOUT_MILLIS = 400L

class ComponentView private constructor(
    private val initial: ComponentViewState
) {

    companion object {

        private val ERROR_COLOR = Color(exceptionBalloonFillColor.rgb)

        fun new(
            scope: CoroutineScope,
            id: ComponentId,
            component: (Flow<PluginMessage>) -> Flow<Started>,
            state: Started
        ): JPanel {

            val initial = state.toViewState(id)

            return ComponentView(initial)
                .apply { scope.launch { render(id, component) } }.root
        }

        private suspend fun ComponentView.render(
            id: ComponentId,
            component: (Flow<PluginMessage>) -> Flow<Started>
        ) = component(messages())
            .map { pluginState -> pluginState.toViewState(id) }
            .collect { componentState -> render(componentState) }

    }

    private lateinit var root: JPanel
    private lateinit var snapshotsTree: JTree
    private lateinit var stateTree: JTree
    private lateinit var searchField: JTextField
    private lateinit var matchCaseCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox
    private lateinit var wordsCheckBox: JCheckBox

    private val snapshotRenderer = SnapshotTreeRenderer(initial.formatter)
    private val snapshotsModel = SnapshotTreeModel.newInstance(initial.component.filteredSnapshots)

    private val stateTreeModel = StateTreeModel.newInstance(initial.component.state)
    private val stateRenderer = StateTreeRenderer(initial.formatter)
    private val transferHandler = TreeRowValueTransferHandler(initial.formatter)

    private fun messages() =
        filterUpdates(initial.component.id, initial.component.filter)
            .mergeWith(snapshotsTree.asOptionMenuUpdates(initial.component.id))

    init {
        snapshotsTree.model = snapshotsModel
        snapshotsTree.transferHandler = transferHandler
        snapshotsTree.selectionModel.selectionMode = TreeSelectionModel.CONTIGUOUS_TREE_SELECTION
        snapshotsTree.cellRenderer = snapshotRenderer

        stateTree.model = stateTreeModel
        stateTree.cellRenderer = stateRenderer
    }

    private fun filterUpdates(
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

    private fun render(
        state: ComponentViewState
    ) {
        updateValues(state)
        updateSearchField(state.component.filter.predicate)
        updateFilterOptions(state.component.filter)
    }

    private fun updateValues(
        state: ComponentViewState
    ) {
        snapshotsModel.swap(state.component.filteredSnapshots)
        stateTreeModel.state = state.component.state

        val newFormatter = state.formatter

        if (newFormatter !== snapshotRenderer.formatter
            || newFormatter !== stateRenderer.formatter
            || newFormatter !== transferHandler.formatter
        ) {
            snapshotRenderer.formatter = newFormatter
            stateRenderer.formatter = newFormatter
            transferHandler.formatter = newFormatter
        }
    }

    private fun updateSearchField(
        validatedPredicate: Validated<Predicate>?
    ) {

        if (validatedPredicate == null || validatedPredicate.isValid()) {
            searchField.background = null
            searchField.toolTipText = null
        } else {
            searchField.background = ERROR_COLOR
            searchField.toolTipText = (validatedPredicate as Invalid).message
        }

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

private inline val ComponentViewState.formatter: ValueFormatter
    get() = if (settings.isDetailedOutput) ::toReadableStringDetailed else ::toReadableStringShort

private fun Started.toViewState(
    id: ComponentId
) = ComponentViewState(debugState.component(id), settings)

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
        is PropertyNode,
        is ValueNode,
        is IndexedNode,
        is EntryKeyNode,
        is EntryValueNode
        -> return // todo modify value at this point
    }

    menu.show(e.component, e.x, e.y)
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

private data class ComponentViewState(
    val component: ComponentDebugState,
    val settings: Settings
)
