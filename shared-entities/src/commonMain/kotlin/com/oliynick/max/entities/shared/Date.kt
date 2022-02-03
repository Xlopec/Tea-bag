package com.oliynick.max.entities.shared

public expect class Date

public expect fun now(): Date

public expect fun fromMillis(
    millis: Long
): Date

public expect fun Date.toMillis(): Long