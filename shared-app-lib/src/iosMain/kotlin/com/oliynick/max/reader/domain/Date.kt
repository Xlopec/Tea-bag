package com.oliynick.max.reader.domain

import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

actual typealias Date = NSDate

actual fun now(): Date = NSDate()

actual fun fromMillis(
    millis: Long
): Date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)

actual fun Date.toMillis(): Long = (timeIntervalSince1970() * 1000.0).toLong()