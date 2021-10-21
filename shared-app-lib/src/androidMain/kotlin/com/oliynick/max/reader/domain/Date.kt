package com.oliynick.max.reader.domain

actual typealias Date = java.util.Date

actual fun now(): Date = java.util.Date()

actual fun Date.toMillis(): Long = time

actual fun fromMillis(
    millis: Long
): Date = java.util.Date(millis)