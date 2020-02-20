package com.oliynick.max.tea.core.debug.gson

import com.google.gson.JsonObject

inline val Class<*>.isJsonPrimitive: Boolean
    get() = kotlin.javaPrimitiveType != null || this == String::class.java

inline val JsonObject.type: Class<*>
    get() = Class.forName(this["@type"].asString)
