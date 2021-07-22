package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.CommonDate
import java.text.SimpleDateFormat
import java.util.*

private val DateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)

actual fun CommonDate.toJson(): String = DateParser.format(impl)

actual fun CommonDate.Companion.fromJson(s: String): CommonDate = CommonDate(DateParser.parse(s))