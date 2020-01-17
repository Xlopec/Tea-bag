package com.oliynick.max.elm.time.travel.converter

import protocol.JsonTree

interface JsonConverter {

    fun <T> toJsonTree(
        any: T
    ): JsonTree

    fun <T> fromJsonTree(
        json: JsonTree,
        cl: Class<T>
    ): T

    fun <T> toJson(
        any: T
    ): String

    fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T
}