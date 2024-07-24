package io.github.xlopec.reader.app.model

import io.github.xlopec.tea.data.Date
import platform.Foundation.NSDateFormatter

private val DateFormatter = NSDateFormatter().apply {
    dateFormat = "dd MMM' at 'hh:mm"
}

public actual fun Date.formatted(): String = DateFormatter.stringFromDate(this)