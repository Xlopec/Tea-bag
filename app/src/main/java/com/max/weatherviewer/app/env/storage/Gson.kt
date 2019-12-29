@file:Suppress("FunctionName")

package com.max.weatherviewer.app.env.storage

import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer

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

