package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.CommonDate
import platform.Foundation.NSDateFormatter

private val DateParser = NSDateFormatter().apply {
    dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
}

actual fun CommonDate.toJson(): String = DateParser.stringFromDate(impl)

actual fun CommonDate.Companion.fromJson(
    s: String
): CommonDate = CommonDate(DateParser.dateFromString(s) ?: error("couldn't parse $s as date"))