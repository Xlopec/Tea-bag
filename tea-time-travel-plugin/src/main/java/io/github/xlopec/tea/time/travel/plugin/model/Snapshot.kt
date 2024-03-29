package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.time.travel.plugin.util.mapNotNull
import kotlinx.collections.immutable.PersistentList

@Immutable
data class OriginalSnapshot(
    val meta: SnapshotMeta,
    val message: Value?,
    val state: Value,
    val commands: CollectionWrapper,
)

@Immutable
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

fun OriginalSnapshot.toFiltered() =
    FilteredSnapshot(
        meta,
        message,
        state,
        commands
    )

fun OriginalSnapshot.filteredBy(
    predicate: Predicate,
): FilteredSnapshot? {

    val m = message?.let { predicate.applyTo(it) }
    val s = predicate.applyTo(state)
    val c = predicate.applyToWrapper(commands)

    return if (m != null || s != null || c != null) {
        FilteredSnapshot(meta, m, s, c)
    } else {
        null
    }
}

fun PersistentList<OriginalSnapshot>.filteredBy(
    predicate: Predicate,
): PersistentList<FilteredSnapshot> =
    mapNotNull { it.filteredBy(predicate) }
