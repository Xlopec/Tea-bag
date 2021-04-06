/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.protocol.JsonConverter

/**
 * Configures and creates a new [converter][GsonConverter] instance
 */
public fun GsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonConverter<JsonElement> = GsonConverter(Gson(config))

private class GsonConverter(
    private val gson: Gson
) : JsonConverter<JsonElement> {

    override fun <T> toJsonTree(
        any: T
    ): JsonElement = gson.toJsonTree(any)

    override fun <T> fromJsonTree(
        json: JsonElement,
        cl: Class<T>
    ): T = gson.fromJson(json, cl)

    override fun <T> toJson(
        any: T
    ): String = gson.toJson(any)

    override fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T = gson.fromJson(json, cl)

}
