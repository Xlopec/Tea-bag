package com.oliynick.max.elm.time.travel.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import protocol.*
import java.util.*

fun gson(config: GsonBuilder.() -> Unit = {}): Gson =
    GsonBuilder()
        .serializeNulls()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .registerTypeAdapter(
            NotifyComponentAttached::class.java,
            NotifyComponentAttachedAdapter
        )
        .registerTypeHierarchyAdapter(
            ServerMessage::class.java,
            ServerMessageAdapter
        )
        .registerTypeHierarchyAdapter(
            ClientMessage::class.java,
            ClientMessageAdapter
        )
        .registerTypeAdapter(
            ApplyMessage::class.java,
            ApplyMessageAdapter
        )
        .registerTypeAdapter(
            NotifyComponentSnapshot::class.java,
            NotifyComponentSnapshotAdapter
        )
        .registerTypeAdapter(
            ApplyState::class.java,
            ApplyStateAdapter
        )
        .registerTypeAdapter(
            ActionApplied::class.java,
            ActionAppliedAdapter
        )
        .registerTypeAdapter(
            UUID::class.java,
            UUIDAdapter
        )
        .registerTypeAdapter(
            ComponentId::class.java,
            ComponentIdAdapter
        )
        .registerTypeHierarchyAdapter(
            List::class.java,
            ListSerializer
        )
        .apply(config)
        .create()

inline operator fun <S, reified T> GsonBuilder.plusAssign(serializer: S) where S : JsonSerializer<T>,
                                                                               S : JsonDeserializer<T> {

    registerTypeHierarchyAdapter(T::class.java, serializer)
}