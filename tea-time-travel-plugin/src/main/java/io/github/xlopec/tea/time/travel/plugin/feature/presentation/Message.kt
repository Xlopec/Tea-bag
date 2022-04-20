package io.github.xlopec.tea.time.travel.plugin.feature.presentation

import io.github.xlopec.tea.time.travel.plugin.UIMessage
import io.github.xlopec.tea.time.travel.plugin.domain.FilterOption
import io.github.xlopec.tea.time.travel.plugin.domain.SnapshotId
import io.github.xlopec.tea.time.travel.protocol.ComponentId

@JvmInline
value class UpdateDebugSettings(
    val isDetailedToStringEnabled: Boolean
) : UIMessage

data class UpdateServerSettings(
    val host: String,
    val port: String
) : UIMessage

data class UpdateFilter(
    val id: ComponentId,
    val input: String,
    val ignoreCase: Boolean,
    val option: FilterOption
) : UIMessage

data class RemoveSnapshots(
    val componentId: ComponentId,
    val ids: Set<SnapshotId>
) : UIMessage {

    constructor(
        componentId: ComponentId,
        id: SnapshotId
    ) : this(componentId, setOf(id))

}

@JvmInline
value class RemoveAllSnapshots(
    val componentId: ComponentId
) : UIMessage

data class ApplyMessage(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : UIMessage

data class ApplyState(
    val componentId: ComponentId,
    val snapshotId: SnapshotId
) : UIMessage

@JvmInline
value class RemoveComponent(
    val componentId: ComponentId
) : UIMessage
