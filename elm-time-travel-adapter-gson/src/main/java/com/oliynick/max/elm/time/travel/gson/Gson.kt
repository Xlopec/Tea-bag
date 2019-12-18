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
            NotifyComponentAttached::class.java,
            NotifyComponentAttachedAdapter
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
        /*.registerTypeAdapter(
            IntWrapper::class.java,
            IntAdapter
        )
        .registerTypeAdapter(
            ByteWrapper::class.java,
            ByteAdapter
        )
        .registerTypeAdapter(
            ShortWrapper::class.java,
            ShortAdapter
        )
        .registerTypeAdapter(
            CharWrapper::class.java,
            CharAdapter
        )
        .registerTypeAdapter(
            DoubleWrapper::class.java,
            DoubleAdapter
        )
        .registerTypeAdapter(
            FloatWrapper::class.java,
            FloatAdapter
        )
        .registerTypeAdapter(
            LongWrapper::class.java,
            LongAdapter
        )
        .registerTypeAdapter(
            BooleanWrapper::class.java,
            BooleanAdapter
        )
        .registerTypeAdapter(
            StringWrapper::class.java,
            StringAdapter
        )
        .registerTypeAdapter(
            CollectionWrapper::class.java,
            CollectionAdapter
        )
        .registerTypeAdapter(
            MapWrapper::class.java,
            MapAdapter
        )
        .registerTypeAdapter(
            Null::class.java,
            NullAdapter
        )
        .registerTypeAdapter(
            Ref::class.java,
            RefAdapter
        )
        .registerTypeHierarchyAdapter(
            Value::class.java,
            ValueDeserializer
        )*/
        .apply(config)
        .create()

inline operator fun <S, reified T> GsonBuilder.plusAssign(serializer: S) where S : JsonSerializer<T>,
                                                                                               S : JsonDeserializer<T> {

    registerTypeHierarchyAdapter(T::class.java, serializer)
}