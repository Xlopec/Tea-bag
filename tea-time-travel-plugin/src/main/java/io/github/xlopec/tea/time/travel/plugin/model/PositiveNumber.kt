package io.github.xlopec.tea.time.travel.plugin.model

@JvmInline
value class PositiveNumber private constructor(
    val value: UInt
) : Comparable<PositiveNumber> {

    companion object {
        val Min = PositiveNumber(1U)
        val Max = PositiveNumber(UInt.MAX_VALUE)

        fun of(
            value: Int
        ) = of(value.toUInt())

        fun of(
            value: UInt
        ) = PositiveNumber(value.coerceAtLeast(Min.value))
    }

    override fun compareTo(
        other: PositiveNumber
    ): Int = value.compareTo(other.value)
}

fun Int.toPositive(): PositiveNumber = PositiveNumber.of(this)

fun UInt.toPositive(): PositiveNumber = PositiveNumber.of(this)

fun PositiveNumber.toInt(): Int = value.toInt()

fun PositiveNumber.toUInt(): UInt = value
