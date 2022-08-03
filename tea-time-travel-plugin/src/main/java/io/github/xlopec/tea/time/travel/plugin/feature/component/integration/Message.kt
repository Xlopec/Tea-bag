package io.github.xlopec.tea.time.travel.plugin.feature.component.integration

import io.github.xlopec.tea.time.travel.plugin.integration.ComponentMessage
import io.github.xlopec.tea.time.travel.plugin.model.FilterOption
import io.github.xlopec.tea.time.travel.plugin.model.PositiveNumber
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotId
import io.github.xlopec.tea.time.travel.protocol.ComponentId

// todo UpdateDebugSettings and UpdateServerSettings should be merged into a single message
data class UpdateDebugSettings(
    val isDetailedToStringEnabled: Boolean,
    val clearSnapshotsOnComponentAttach: Boolean,
    val maxSnapshots: PositiveNumber,
) : ComponentMessage

data class UpdateServerSettings(
    val host: String,
    val port: String
) : ComponentMessage

data class UpdateFilter(
    val id: ComponentId,
    val input: String,
    val ignoreCase: Boolean,
    val option: FilterOption
) : ComponentMessage

data class RemoveSnapshots(
    val componentId: ComponentId,
    val ids: Set<SnapshotId>
) : ComponentMessage {

    constructor(
        componentId: ComponentId,
        id: SnapshotId
    ) : this(componentId, setOf(id))
}

@JvmInline
value class RemoveAllSnapshots(
    val componentId: ComponentId
) : ComponentMessage

data class ApplyMessage(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : ComponentMessage

data class ApplyState(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : ComponentMessage

@JvmInline
value class RemoveComponent(
    val id: ComponentId
) : ComponentMessage

@JvmInline
value class SelectComponent(
    val id: ComponentId
) : ComponentMessage
