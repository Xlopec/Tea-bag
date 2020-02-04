package com.oliynick.max.elm.time.travel.gson

import com.google.gson.JsonObject

inline val Class<*>.isJsonPrimitive: Boolean
    get() = kotlin.javaPrimitiveType != null || this == String::class.java

inline val JsonObject.type: Class<*>
    get() = Class.forName(this["@type"].asString)
