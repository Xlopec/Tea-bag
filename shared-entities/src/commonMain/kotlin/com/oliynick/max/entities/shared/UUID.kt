package com.oliynick.max.entities.shared

expect class UUID

expect fun randomUUID(): UUID

expect fun String.toUUID(): UUID

expect fun UUID.toHumanReadable(): String