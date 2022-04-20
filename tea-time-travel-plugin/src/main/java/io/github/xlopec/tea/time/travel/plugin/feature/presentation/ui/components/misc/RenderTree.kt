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

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc

import io.github.xlopec.tea.time.travel.plugin.domain.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.domain.Property
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.domain.Value

sealed interface RenderTree

object RootNode : RenderTree

data class SnapshotNode(
    val snapshot: FilteredSnapshot
) : RenderTree

data class MessageNode(
    val id: SnapshotId,
    val message: Value
) : RenderTree

data class StateNode(
    val id: SnapshotId,
    val state: Value
) : RenderTree

data class PropertyNode(
    val property: Property
) : RenderTree

data class ValueNode(
    val value: Value
) : RenderTree

data class IndexedNode(
    val index: Int,
    val value: Value
) : RenderTree

data class EntryKeyNode(
    val key: Value
) : RenderTree

data class EntryValueNode(
    val value: Value
) : RenderTree
