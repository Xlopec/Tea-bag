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

package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.component

import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.EntryKeyNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.EntryValueNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.IndexedNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.MessageNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.PropertyNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.RenderTree
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.RootNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.SnapshotNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.StateNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.ValueFormatter
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.ValueNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.icon
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.toReadableString
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeModel

class RenderTreeRenderer private constructor(
    var formatter: ValueFormatter,
    private val transformer: RenderTree.(TreeModel, ValueFormatter) -> String,
) : TreeCellRenderer {

    companion object {

        fun SnapshotsRenderer(
            formatter: ValueFormatter,
        ) = RenderTreeRenderer(formatter, ::toReadableSnapshotString)

        fun StateRenderer(
            formatter: ValueFormatter,
        ) = RenderTreeRenderer(formatter) { _, f -> toReadableStateString(this, f) }
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component =
        JLabel().apply {

            val payload = (value as DefaultMutableTreeNode).userObject as RenderTree

            text = transformer(payload, tree.model, formatter)
            icon = payload.icon
        }

}


private fun toReadableStateString(
    renderTree: RenderTree,
    formatter: ValueFormatter,
): String =
    when (renderTree) {
        RootNode -> "State"
        is SnapshotNode, is MessageNode, is StateNode -> error("Can't render $renderTree")
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }

private fun toReadableSnapshotString(
    renderTree: RenderTree,
    model: TreeModel,
    formatter: ValueFormatter,
): String =
    when (renderTree) {
        RootNode -> "Snapshots (${model.getChildCount(model.root)})"
        is SnapshotNode -> renderTree.snapshot.toReadableString()
        is MessageNode -> "Message"
        is StateNode -> "State"
        is PropertyNode -> renderTree.toReadableString(formatter)
        is ValueNode -> renderTree.toReadableString(formatter)
        is IndexedNode -> renderTree.toReadableString(formatter)
        is EntryKeyNode -> renderTree.toReadableString(formatter)
        is EntryValueNode -> renderTree.toReadableString(formatter)
    }
