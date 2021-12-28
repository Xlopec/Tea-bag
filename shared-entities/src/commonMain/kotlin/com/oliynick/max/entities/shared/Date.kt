package com.oliynick.max.entities.shared

expect class Date

expect fun now(): Date

expect fun fromMillis(
    millis: Long
): Date

expect fun Date.toMillis(): Long