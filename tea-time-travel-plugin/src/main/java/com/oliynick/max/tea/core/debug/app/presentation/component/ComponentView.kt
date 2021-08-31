/*
 * Copyright (C) 2021. Maksym Oliinyk.
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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.javaPsiFacade
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.*
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.ActionIcons.RemoveIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.ActionIcons.UpdateRunningAppIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.misc.ValueIcon.ClassIcon
import com.oliynick.max.tea.core.debug.app.presentation.ui.ErrorColor
import com.oliynick.max.tea.core.debug.app.presentation.ui.InputTimeoutMillis
import com.oliynick.max.tea.core.debug.app.presentation.ui.action.DefaultMouseListener
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.TreeSelectionModel

class ComponentView private constructor(
    private val initial: ComponentViewState,
    private val id: ComponentId,
    private val project: Project,
    private val component: (Flow<PluginMessage>) -> Flow<Started>,
    scope: CoroutineScope
) : CoroutineScope by scope {

    companion object {

        fun new(
            scope: CoroutineScope,
            id: ComponentId,
            project: Project,
            component: (Flow<PluginMessage>) -> Flow<Started>,
            state: Started
        ): ComponentView {

            val initial = state.toViewState(id)

            return ComponentView(initial, id, project, component, scope)
                .apply { scope.launch { render(id, component) } }
        }

    }

    lateinit var root: JPanel
        private set

    private lateinit var snapshotsTree: JTree
    private lateinit var stateTree: JTree
    private lateinit var searchField: JTextField
    private lateinit var matchCaseCheckBox: JCheckBox
    private lateinit var regexCheckBox: JCheckBox
    private lateinit var wordsCheckBox: JCheckBox

    private val snapshotRenderer = RenderTreeRenderer.SnapshotsRenderer(initial.formatter)
    private val snapshotsModel = SnapshotTreeModel.newInstance(initial.component.filteredSnapshots)

    private val stateTreeModel = StateTreeModel.newInstance(initial.component.state)
    private val stateRenderer = RenderTreeRenderer.StateRenderer(initial.formatter)
    private val transferHandler = TreeRowValueTransferHandler(initial.formatter)

    init {
        snapshotsTree.model = snapshotsModel
        snapshotsTree.transferHandler = transferHandler
        snapshotsTree.selectionModel.selectionMode = TreeSelectionModel.CONTIGUOUS_TREE_SELECTION
        snapshotsTree.cellRenderer = snapshotRenderer

        stateTree.model = stateTreeModel
        stateTree.cellRenderer = stateRenderer
    }

    init {
        launch {
            component(messages())
                .map { pluginState -> pluginState.toViewState(id) }
                .collect { componentState -> render(componentState) }
        }
    }

    private suspend fun render(
        id: ComponentId,
        component: (Flow<PluginMessage>) -> Flow<Started>
    ) = component(messages())
        .map { pluginState -> pluginState.toViewState(id) }
        .collect { componentState -> render(componentState) }

    private fun messages() =
        filterUpdates(initial.component.id, initial.component.filter)
            .mergeWith(snapshotsTree.asOptionMenuUpdates(initial.component.id, project))

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

        return filterUpdates(
            id,
            searchField.textFlow(),
            matchCaseCheckBox.asMatchCaseFlow(filter),
            optionsChanges
        )
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
            searchField.background = ErrorColor
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
) = combine(
    textChanges,
    ignoreCaseChanges,
    optionsChanges
) { text, ignoreCase, option -> UpdateFilter(id, text, ignoreCase, option) }

private fun substringFlow(
    regexFlow: Flow<FilterOption?>,
    wordsFlow: Flow<FilterOption?>
) =
    regexFlow.combine(wordsFlow) { reg, word -> if (reg === word && reg == null) FilterOption.SUBSTRING else null }

private fun JTree.asOptionMenuUpdates(
    id: ComponentId,
    project: Project
): Flow<PluginMessage> =
    callbackFlow {

        val l = object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showActionPopup(e, id, project) { offer(it) }
                }
            }
        }

        addMouseListener(l)

        awaitClose { removeMouseListener(l) }
    }

private fun JTextField.textFlow() =
    textChanges()
        .onStart { emit("") }
        .debounce(InputTimeoutMillis)

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
    project: Project,
    onAction: (PluginMessage) -> Unit
) {
    val row = getClosestRowForLocation(e.x, e.y)

    setSelectionRow(row)

    val menu: JPopupMenu? = when (val treeNode = getSubTreeForRow(row)) {
        is SnapshotNode -> SnapshotPopup(id, treeNode.snapshot, onAction)
        is MessageNode -> MessagePopup(id, treeNode.id, onAction)
        is StateNode -> StatePopup(id, treeNode.id, onAction)
        RootNode -> SnapshotsPopup(id, onAction)
        is ValueNode -> ValuePopup(project, treeNode.value)
        is PropertyNode,
        is IndexedNode,
        is EntryKeyNode,
        is EntryValueNode -> null // todo modify value at this point
    }

    menu?.show(e.component, e.x, e.y)
}

private fun ValuePopup(
    project: Project,
    value: Value
): JPopupMenu? {
    val facade = project.javaPsiFacade

    val psiClass = (value as? Ref)
        ?.let { ref -> facade.findClass(ref.type.name, GlobalSearchScope.projectScope(project)) }
        ?: return null

    return JBPopupMenu("Actions").apply {
        add(JBMenuItem("Jump to sources", ClassIcon).apply {
            addActionListener {
                PsiNavigateUtil.navigate(psiClass)
            }
        })
    }
}

private fun SnapshotsPopup(
    id: ComponentId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu = JBPopupMenu("Snapshots").apply {
    add(JBMenuItem("Delete all", RemoveIcon).apply {
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
            UpdateRunningAppIcon
        ).apply {
            addActionListener {
                onAction(ApplyState(component, snapshot.meta.id))
            }
        })

        add(JBMenuItem("Delete", RemoveIcon).apply {
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
        add(JBMenuItem("Apply this message", UpdateRunningAppIcon).apply {
            addActionListener {
                onAction(ApplyMessage(componentId, snapshotId))
            }
        })
    }

private fun StatePopup(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    onAction: (PluginMessage) -> Unit
): JPopupMenu =
    JBPopupMenu().apply {
        add(JBMenuItem("Apply this state", UpdateRunningAppIcon).apply {
            addActionListener {
                onAction(ApplyState(componentId, snapshotId))
            }
        })
    }

private data class ComponentViewState(
    val component: ComponentDebugState,
    val settings: Settings
)
