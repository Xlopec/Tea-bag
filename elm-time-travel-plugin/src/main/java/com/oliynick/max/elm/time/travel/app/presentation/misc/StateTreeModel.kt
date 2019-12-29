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

import com.oliynick.max.elm.time.travel.app.domain.cms.Value
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeModel

class StateTreeModel private constructor(private val delegate: DefaultTreeModel,
                                         initial: Value<*>) : TreeModel by delegate {

    companion object {
        fun newInstance(state: Value<*>): StateTreeModel {
            return StateTreeModel(DefaultTreeModel(DefaultMutableTreeNode("State", true)), state)
        }
    }

    var state: Value<*> = initial
        set(value) {
            if (value !== field) {
                field = value

                val root = delegate.root as MutableTreeNode

                if (root.childCount > 0) {
                    delegate.removeNodeFromParent(root.getChildAt(0) as MutableTreeNode)
                }
                delegate.insertNodeInto(value.toJTree(), root, 0)
            }
        }

}