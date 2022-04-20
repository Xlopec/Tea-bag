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

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.component

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.time.travel.plugin.Message
import io.github.xlopec.tea.time.travel.plugin.domain.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.domain.Filter
import io.github.xlopec.tea.time.travel.plugin.domain.FilterOption
import io.github.xlopec.tea.time.travel.plugin.domain.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.Invalid
import io.github.xlopec.tea.time.travel.plugin.domain.Predicate
import io.github.xlopec.tea.time.travel.plugin.domain.Ref
import io.github.xlopec.tea.time.travel.plugin.domain.Settings
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.domain.Validated
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.plugin.domain.isValid
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ApplyState
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.RemoveAllSnapshots
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.RemoveSnapshots
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.UpdateFilter
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ActionIcons.RemoveIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ActionIcons.UpdateRunningAppIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ErrorColor
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.InputTimeoutMillis
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.ValueIcon.ClassIcon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.action.DefaultMouseListener
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.EntryKeyNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.EntryValueNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.IndexedNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.MessageNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.PropertyNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.RootNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.SnapshotNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.StateNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.TreeRowValueTransferHandler
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.ValueFormatter
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.ValueNode
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.getSubTreeForRow
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.mergeWith
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.selections
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.textChanges
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.textSafe
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.toReadableStringDetailed
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc.toReadableStringShort
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.screens.component.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.misc.javaPsiFacade
import io.github.xlopec.tea.time.travel.plugin.state.Started
import io.github.xlopec.tea.time.travel.plugin.state.component
import java.awt.event.MouseEvent
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.TreeSelectionModel
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

class ComponentView private constructor(
    private val initial: ComponentViewState,
    private val id: ComponentId,
    private val project: Project,
    private val component: (Flow<Message>) -> Flow<Started>,
    scope: CoroutineScope
) : CoroutineScope by scope {

    companion object {

        fun new(
            scope: CoroutineScope,
            id: ComponentId,
            project: Project,
            component: (Flow<Message>) -> Flow<Started>,
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
        component: (Flow<Message>) -> Flow<Started>
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
): Flow<Message> =
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
    onAction: MessageHandler
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
    onAction: MessageHandler
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
    onAction: MessageHandler
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
    onAction: MessageHandler
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
    onAction: MessageHandler
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
