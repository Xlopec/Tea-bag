package io.github.xlopec.tea.time.travel.gson.metadata

import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

internal class WildcardTypeImpl(
    private val upperBounds: Array<Type>,
    private val lowerBounds: Array<Type>
) : WildcardType {

    init {
        require(lowerBounds.size <= 1)
        require(upperBounds.size == 1)
    }

    override fun getUpperBounds(): Array<Type> {
        return upperBounds
    }

    override fun getLowerBounds(): Array<Type> {
        return lowerBounds
    }

    override fun equals(other: Any?): Boolean {
        return other is WildcardType && `$Gson$Types`.equals(this, other as WildcardType?)
    }

    override fun hashCode(): Int {
        var result = upperBounds.contentHashCode()
        result = 31 * result + lowerBounds.contentHashCode()
        return result
    }


}