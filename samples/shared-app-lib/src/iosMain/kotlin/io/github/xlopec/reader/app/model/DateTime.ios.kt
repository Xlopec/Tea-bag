package io.github.xlopec.reader.app.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

private val DateFormatter = NSDateFormatter().apply {
    dateFormat = "dd MMM' at 'HH:mm"
}

public actual fun LocalDateTime.formatted(): String = DateFormatter.stringFromDate(toNSDate())

private fun LocalDateTime.toNSDate(): NSDate {
    return NSCalendar.currentCalendar.dateFromComponents(toNSDateComponents()) ?: error("Couldn't convert $this to NSDate")
}
