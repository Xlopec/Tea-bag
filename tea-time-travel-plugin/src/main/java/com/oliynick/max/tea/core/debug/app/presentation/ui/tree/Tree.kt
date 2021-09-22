/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.tea.core.debug.app.presentation.ui.tree

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import com.oliynick.max.tea.core.debug.app.domain.*

@Stable
data class Tree(
    val root: Node,
    val selected: MutableState<Node?> = mutableStateOf(null),
)

@Stable
sealed interface Node

@JvmInline
@Immutable
value class Leaf(
    val value: Value,
) : Node

@Stable
data class RefNode(
    val type: Type,
    val children: Collection<PropertyNode1>,
    val expanded: MutableState<Boolean> = mutableStateOf(false),
) : Node

@Stable
data class PropertyNode1(
    val name: String,
    val node: Node,
)

@Stable
data class CollectionNode(
    val children: List<Node>,
    val expanded: MutableState<Boolean> = mutableStateOf(false),
) : Node

//todo rework
fun Value.toRenderTree(expanded: Boolean = true): Node =
    when (this) {
        is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper, Null -> Leaf(this)
        is CollectionWrapper -> CollectionNode(
            value.map { it.toRenderTree() },
            mutableStateOf(expanded)
        )
        is Ref -> RefNode(
            type,
            properties.map { PropertyNode1(it.name, it.v.toRenderTree()) },
            mutableStateOf(expanded)
        )
    }