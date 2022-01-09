package com.oliynick.max.entities.shared

public actual typealias Date = java.util.Date

public actual fun now(): Date = java.util.Date()

public actual fun fromMillis(
    millis: Long
): Date = java.util.Date(millis)

public actual fun Date.toMillis(): Long = time