package com.oliynick.max.tea.core.debug.protocol

/**
 * Platform agnostic UUID contract
 */
public expect class UUID {

    public companion object {
        /**
         * Factory to obtain random UUID
         */
        public fun randomUUID(): UUID

        public fun fromString(
            rawUUID: String
        ): UUID
    }

    /**
     * Returns unique human readable representation of this UUID
     */
    public fun toHumanReadable(): String
}