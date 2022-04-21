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

/**
 * This custom tree view implementation is a temp solution, it'll be removed
 * when officially supported composable tree view implementation is released
 */
package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.tree

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import io.github.xlopec.tea.time.travel.plugin.domain.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.Null
import io.github.xlopec.tea.time.travel.plugin.domain.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.Ref
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotMeta
import io.github.xlopec.tea.time.travel.plugin.domain.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.domain.Type
import io.github.xlopec.tea.time.travel.plugin.domain.Value
import io.github.xlopec.tea.time.travel.protocol.ComponentId

// todo: consider bottom-up value tree modification
// assumption TreeState <-> Value (isomorphic)
// e.g. modify Leaf Node -> construct a new Value tree by applying modification from Tree state -> replace old Value tree with new one

@Stable
data class TreeState(
    val id: ComponentId,
    val roots: List<Node>,
    val selected: MutableState<Node?> = mutableStateOf(null),
) {
    constructor(id: ComponentId, root: Node, selected: MutableState<Node?> = mutableStateOf(null)) :
            this(id, listOf(root), selected)
}

@Stable
sealed interface Node {
    // fixme temp shitty workaround
    val id: Int
}

@Stable
sealed interface INode : Node {
    val expanded: MutableState<Boolean>
}

@Immutable
data class Leaf(
    override val id: Int,
    val value: Value,
) : Node

@Stable
data class SnapshotINode(
    override val id: Int,
    val meta: SnapshotMeta,
    val message: Node?,
    val state: Node?,
    val commands: CollectionNode?,
    override val expanded: MutableState<Boolean> = mutableStateOf(false),
) : INode

@Stable
data class RefNode(
    override val id: Int,
    val type: Type,
    val children: Collection<PropertyNode>,
    override val expanded: MutableState<Boolean> = mutableStateOf(false),
) : INode

@Stable
data class PropertyNode(
    val name: String,
    val node: Node,
)

@Stable
data class CollectionNode(
    override val id: Int,
    val children: List<Node>,
    override val expanded: MutableState<Boolean> = mutableStateOf(false),
) : INode

private fun CollectionWrapper.toRenderTree(
    id: Int,
    expanded: Boolean,
): CollectionNode {
    var i = id + 1

    return CollectionNode(
        id,
        items.map { value ->
            value.toRenderTree(i, expanded)
                .also { i += it.childrenCount + 1 }
        },
        mutableStateOf(expanded)
    )
}

fun Iterable<FilteredSnapshot>.toRenderTree(
    expanded: Boolean = false,
): List<SnapshotINode> {

    var i = 0

    return map { it.toRenderTree(i, expanded).also { i += it.childrenCount + 1 } }
}

fun FilteredSnapshot.toRenderTree(
    id: Int,
    expanded: Boolean,
): SnapshotINode {

    var i = id + 1

    return SnapshotINode(
        id,
        meta,
        message?.toRenderTree(i)?.also { i += it.childrenCount + 1 },
        state?.toRenderTree(i)?.also { i += it.childrenCount + 1 },
        commands?.toRenderTree(i, expanded),
        mutableStateOf(expanded)
    )
}

private fun Ref.toRenderTree(
    id: Int,
    expanded: Boolean,
): RefNode {
    var i = id + 1

    return RefNode(
        id,
        type,
        properties.map { (name, value) ->
            PropertyNode(name, value.toRenderTree(i, expanded)
                .also { i += it.childrenCount + 1 })
        },
        mutableStateOf(expanded)
    )
}

// todo rework
fun Value.toRenderTree(id: Int = 0, expanded: Boolean = true): Node =
    when (this) {
        is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper, Null -> Leaf(
            id,
            this
        )
        is CollectionWrapper -> toRenderTree(id, expanded)
        is Ref -> toRenderTree(id, expanded)
    }

val Node.childrenCount: Int
    get() =
        when (this) {
            is CollectionNode -> children.fold(children.size) { acc, p -> acc + p.childrenCount }
            is Leaf -> 0
            is RefNode -> children.fold(children.size) { acc, p -> acc + p.node.childrenCount }
            is SnapshotINode -> 2 // message and state properties
        }
