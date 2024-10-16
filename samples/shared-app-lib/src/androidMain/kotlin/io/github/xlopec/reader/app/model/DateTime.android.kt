package io.github.xlopec.reader.app.model

import android.annotation.SuppressLint
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.ZoneId
import java.util.*
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

@SuppressLint("ConstantLocale")
private val DateTimeFormatter: JavaDateTimeFormatter = JavaDateTimeFormatter.ofPattern("dd MMM' at 'hh:mm", Locale.getDefault())

public actual fun LocalDateTime.formatted(): String {
    val time1 = toJavaLocalDateTime().atZone(ZoneId.systemDefault())
    return DateTimeFormatter.format(time1)
}
