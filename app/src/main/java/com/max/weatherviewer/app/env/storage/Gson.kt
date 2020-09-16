@file:Suppress("FunctionName")

package com.max.weatherviewer.app.env.storage

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

