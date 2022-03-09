package com.oliynick.max.reader.app.misc

fun String.coerceIn(
    min: UInt,
    max: UInt
): String? = take(max.toInt()).takeIf { it.length.toUInt() in min..max }
