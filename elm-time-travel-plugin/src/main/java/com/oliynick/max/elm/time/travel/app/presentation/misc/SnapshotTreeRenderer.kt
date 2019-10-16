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

package com.oliynick.max.elm.time.travel.app.presentation.misc

import java.awt.Component
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeCellRenderer

object SnapshotTreeRenderer : TreeCellRenderer {

    override fun getTreeCellRendererComponent(tree: JTree,
                                              value: Any,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component {

        val label = JLabel()
        val payload = (value as DefaultMutableTreeNode).userObject as SnapshotTree

        label.text = when(payload) {
            RootNode -> "Snapshots (${tree.model.getChildCount(tree.model.root)})"
            is SnapshotNode -> payload.snapshot.toReadableString()
            is MessageNode -> "Message"
            is StateNode -> "State"
            is SnapshotTypeNode -> payload.typeNode.toReadableString()
        }

        label.icon = when(payload) {
            RootNode, is MessageNode, is SnapshotNode, is StateNode -> null
            is SnapshotTypeNode -> payload.typeNode.icon
        }

        return label
    }

}