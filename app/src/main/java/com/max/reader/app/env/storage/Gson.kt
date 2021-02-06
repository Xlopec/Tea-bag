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

package com.max.reader.app.env.storage

import com.google.gson.*

interface HasGson {
    val gson: Gson
}

interface TypeAdapter<T> : JsonSerializer<T>, JsonDeserializer<T>

fun Gson(gson: Gson) = object : HasGson {
    override val gson: Gson = gson
}

fun Gson(gson: () -> Gson) = object : HasGson {
    override val gson: Gson by lazy(gson)
}

