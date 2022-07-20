package io.github.xlopec.tea.time.travel.plugin.model

@JvmInline
value class PositiveNumber private constructor(
    val value: UInt
) : Comparable<PositiveNumber> {

    companion object {
        const val Min = 1U
        const val Max = UInt.MAX_VALUE

        fun of(
            value: Int
        ) = of(value.toUInt())

        fun of(
            value: UInt
        ) = PositiveNumber(value.coerceAtLeast(Min))
    }

    override fun compareTo(
        other: PositiveNumber
    ): Int = value.compareTo(other.value)
}
