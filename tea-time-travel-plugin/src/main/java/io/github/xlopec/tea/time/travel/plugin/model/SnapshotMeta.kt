package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class SnapshotMeta(
    val id: SnapshotId,
    val timestamp: LocalDateTime
)
