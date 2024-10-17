package io.github.xlopec.reader.app.model

import android.annotation.SuppressLint
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.util.*
import java.time.format.DateTimeFormatter as JavaDateTimeFormatter

@SuppressLint("ConstantLocale")
private val DateTimeFormatter: JavaDateTimeFormatter = JavaDateTimeFormatter.ofPattern("dd MMM' at 'HH:mm", Locale.getDefault())

public actual fun LocalDateTime.formatted(): String {
    return DateTimeFormatter.format(toJavaLocalDateTime())
}
