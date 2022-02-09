package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.entities.shared.Date
import platform.Foundation.NSDateFormatter

private val DateParser = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}

actual fun Date.toJson(): String = DateParser.stringFromDate(this)

actual fun String.toDate(): Date = DateParser.dateFromString(this)
    ?: error("couldn't parse $this as date")