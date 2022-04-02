package com.oliynick.max.tea.data

public expect class Date

public expect fun now(): Date

public expect fun fromMillis(
    millis: Long
): Date

public expect fun Date.toMillis(): Long