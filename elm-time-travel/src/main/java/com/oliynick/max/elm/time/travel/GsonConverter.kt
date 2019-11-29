package com.oliynick.max.elm.time.travel

import com.oliynick.max.elm.time.travel.gson.gson

internal object GsonConverter : JsonConverter {

    private val gson = gson()

    override fun toJson(any: Any): String = gson.toJson(any)

    override fun <T> fromJson(json: String, cl: Class<T>): T = gson.fromJson(json, cl)

}