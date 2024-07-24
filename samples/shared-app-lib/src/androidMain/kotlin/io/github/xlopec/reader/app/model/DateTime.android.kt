package io.github.xlopec.reader.app.model

import io.github.xlopec.tea.data.Date
import java.text.SimpleDateFormat
import java.util.*

private val DateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}

actual fun Date.formatted(): String = DateFormatter.format(this)