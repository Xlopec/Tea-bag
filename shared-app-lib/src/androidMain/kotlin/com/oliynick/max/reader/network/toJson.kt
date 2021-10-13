package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.Date
import java.text.SimpleDateFormat
import java.util.*

private val DateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)

actual fun Date.toJson(): String = DateParser.format(this)

actual fun String.toDate(): Date = DateParser.parse(this) ?: error("Invalid date $this")