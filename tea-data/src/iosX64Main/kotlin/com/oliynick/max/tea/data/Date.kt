package com.oliynick.max.tea.data

import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

public actual typealias Date = NSDate

public actual fun now(): Date = NSDate()

public actual fun fromMillis(
    millis: Long
): Date = NSDate.dateWithTimeIntervalSince1970(millis / 1000.0)

public actual fun Date.toMillis(): Long = (timeIntervalSince1970() * 1000.0).toLong()