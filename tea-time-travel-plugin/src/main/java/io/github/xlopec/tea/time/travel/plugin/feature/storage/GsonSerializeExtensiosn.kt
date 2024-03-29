package io.github.xlopec.tea.time.travel.plugin.feature.storage

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.data.toHumanReadable
import io.github.xlopec.tea.time.travel.plugin.feature.server.toCollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.feature.server.toJsonArray
import io.github.xlopec.tea.time.travel.plugin.feature.server.toJsonElement
import io.github.xlopec.tea.time.travel.plugin.feature.server.toValue
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotMeta
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

internal fun JsonObject.toComponentDebugState(): DebuggableComponent =
    DebuggableComponent(
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

internal fun DebuggableComponent.toJsonObject() = JsonObject().apply {
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
