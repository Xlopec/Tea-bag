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

package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.feature.component.model.ComponentState
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.collections.immutable.PersistentMap

typealias ComponentMapping = PersistentMap<ComponentId, ComponentState>

@JvmInline
value class SnapshotId(
    val value: UUID
)

data class SnapshotMeta(
    val id: SnapshotId,
    val timestamp: LocalDateTime
)

data class OriginalSnapshot(
    val meta: SnapshotMeta,
    val message: Value?,
    val state: Value,
    val commands: CollectionWrapper,
)

data class FilteredSnapshot(
    val meta: SnapshotMeta,
    val message: Value? = null,
    val state: Value? = null,
    val commands: CollectionWrapper? = null,
) {
    init {
        require(message != null || state != null || commands != null) { "failed requirement $this" }
    }
}
