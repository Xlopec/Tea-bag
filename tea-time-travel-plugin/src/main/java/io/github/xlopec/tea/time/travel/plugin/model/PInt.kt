package io.github.xlopec.tea.time.travel.plugin.model

/**
 * Denotes positive integer - x > 0
 */
@JvmInline
value class PInt private constructor(
    val value: UInt
) : Comparable<PInt> {

    companion object {
        val MIN_VALUE = PInt(1U)
        val MAX_VALUE = PInt(UInt.MAX_VALUE)

        fun of(
            value: Int
        ) = of(value.toUInt())

        fun of(
            value: UInt
        ) = PInt(value.coerceAtLeast(MIN_VALUE.value))
    }

    override fun compareTo(
        other: PInt
    ): Int = value.compareTo(other.value)
}

fun Int.toPInt(): PInt = PInt.of(this)

fun UInt.toPInt(): PInt = PInt.of(this)

fun PInt.toInt(): Int = value.toInt()

fun PInt.toUInt(): UInt = value

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.plus(
    other: PInt
) = (value + other.value).toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.minus(
    other: PInt
) = (value - other.value).toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.times(
    other: PInt
) = (value * other.value).toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.div(
    other: PInt
) = (value / other.value).toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.rem(
    other: PInt
) = (value % other.value).toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.inc() = value.inc().toPInt()

@Suppress("NOTHING_TO_INLINE")
inline operator fun PInt.dec() = value.dec().toPInt()
