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

import com.oliynick.max.tea.core.debug.app.presentation.misc.RenderTree
import com.oliynick.max.tea.core.debug.app.presentation.misc.ValueFormatter
import com.oliynick.max.tea.core.debug.app.presentation.misc.icon
import com.oliynick.max.tea.core.debug.app.presentation.misc.toReadableString
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

class SnapshotTreeRenderer(
    var formatter: ValueFormatter
) : TreeCellRenderer {

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component =
        JLabel().apply {
            val payload = (value as DefaultMutableTreeNode).userObject as RenderTree

            text = payload.toReadableString(tree.model, formatter)
            icon = payload.icon
        }

}