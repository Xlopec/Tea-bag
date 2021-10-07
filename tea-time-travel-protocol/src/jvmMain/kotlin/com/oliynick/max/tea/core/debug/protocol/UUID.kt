package com.oliynick.max.tea.core.debug.protocol

import java.util.UUID as JavaUUID

/**
 * Jvm-specific implementation of UUID
 */
public actual class UUID private constructor(
    private val delegate: JavaUUID,
) {

    public actual companion object {
        public actual fun randomUUID(): UUID = UUID(JavaUUID.randomUUID())

        public actual fun fromString(
            rawUUID: String
        ): UUID = UUID(JavaUUID.fromString(rawUUID))
    }

    override fun equals(other: Any?): Boolean =
        if (other is UUID) delegate == other.delegate
        else false

    override fun toString(): String = delegate.toString()

    override fun hashCode(): Int = delegate.hashCode()

    public actual fun toHumanReadable(): String = delegate.toString()

}