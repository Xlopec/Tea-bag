package com.oliynick.max.entities.shared

actual typealias Date = java.util.Date

actual fun now(): Date = java.util.Date()

actual fun fromMillis(
    millis: Long
): Date = java.util.Date(millis)

actual fun Date.toMillis(): Long = time