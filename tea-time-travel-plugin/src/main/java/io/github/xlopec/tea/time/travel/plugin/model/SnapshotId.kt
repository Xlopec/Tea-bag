package io.github.xlopec.tea.time.travel.plugin.model

import kotlin.uuid.Uuid

@JvmInline
value class SnapshotId(
    val value: Uuid
)
