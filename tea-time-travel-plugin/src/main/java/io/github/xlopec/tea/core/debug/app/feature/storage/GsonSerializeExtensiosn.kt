package io.github.xlopec.tea.core.debug.app.feature.storage

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.xlopec.tea.core.debug.app.domain.ComponentDebugState
import io.github.xlopec.tea.core.debug.app.domain.OriginalSnapshot
import io.github.xlopec.tea.core.debug.app.domain.SnapshotId
import io.github.xlopec.tea.core.debug.app.domain.SnapshotMeta
import io.github.xlopec.tea.core.debug.app.feature.server.toCollectionWrapper
import io.github.xlopec.tea.core.debug.app.feature.server.toJsonArray
import io.github.xlopec.tea.core.debug.app.feature.server.toJsonElement
import io.github.xlopec.tea.core.debug.app.feature.server.toValue
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.data.toHumanReadable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

internal fun JsonObject.toComponentDebugState(): ComponentDebugState =
    ComponentDebugState(
        ComponentId(this["id"].asString),
        this["state"].toValue(),
        this["snapshots"].asJsonArray.toSnapshots()
    )

internal fun JsonObject.toSnapshotMeta() =
    SnapshotMeta(
        SnapshotId(UUID.fromString(this["uuid"].asString)),
        LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(this["timestamp"].asString))
    )

internal fun JsonObject.toOriginalSnapshot(): OriginalSnapshot {
    val meta = this["meta"].asJsonObject.toSnapshotMeta()

    val message = this["message"]?.toValue()
    val state = this["state"].toValue()
    val commands = this["commands"].asJsonArray.toCollectionWrapper()

    return OriginalSnapshot(
        meta = meta,
        message = message,
        state = state,
        commands = commands
    )
}

internal fun JsonArray.toSnapshots() =
    persistentListOf<OriginalSnapshot>().mutate { l ->
        forEach { jsonElement ->
            l += jsonElement.asJsonObject.toOriginalSnapshot()
        }
    }

internal fun ComponentDebugState.toJsonObject() = JsonObject().apply {
    addProperty("id", id.value)
    // should we really store state?
    add("state", state.toJsonElement())
    add("snapshots", snapshots.toJsonArray(OriginalSnapshot::toJsonElement))
}

internal fun OriginalSnapshot.toJsonElement() = JsonObject().apply {
    add("meta", meta.toJsonObject())
    add("state", state.toJsonElement())
    add("commands", commands.toJsonArray())
    if (message != null) {
        // don't serialize null message or it'll be improperly serialized to Null
        add("message", message.toJsonElement())
    }
}

internal fun SnapshotMeta.toJsonObject() = JsonObject().apply {
    addProperty("uuid", id.value.toHumanReadable())
    addProperty("timestamp", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp))
}