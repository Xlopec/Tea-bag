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

package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.component

import io.github.xlopec.tea.core.debug.app.domain.Value
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.RootNode
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.misc.toJTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class StateTreeModel private constructor(
    private val delegate: DefaultTreeModel,
    initial: Value
) : TreeModel by delegate {

    companion object {
        fun newInstance(state: Value): StateTreeModel =
            StateTreeModel(DefaultTreeModel(DefaultMutableTreeNode(RootNode, true))
                .apply { swap(state) }, state)
    }

    var state: Value = initial
        set(value) {
            if (value != field) {
                field = value
                delegate.swap(value)
            }
        }

}

private fun DefaultTreeModel.swap(
    value: Value
) {
    val root = root as MutableTreeNode

    if (root.childCount > 0) {
        removeNodeFromParent(root.getChildAt(0) as MutableTreeNode)
    }
    insertNodeInto(value.toJTree(), root, 0)
}
