package com.oliynick.max.elm.time.travel.gson.serialization.data

import kotlinx.collections.immutable.PersistentList

data class Container(val list: PersistentList<String>)

data class PolyContainer(val list: PersistentList<Poly>)

interface Poly {
    val property: String
}

class PolyB : Poly {
    override val property: String = "b"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolyB

        if (property != other.property) return false

        return true
    }

    override fun hashCode(): Int {
        return property.hashCode()
    }
}

class PolyA : Poly {
    override val property: String = "a"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolyA

        if (property != other.property) return false

        return true
    }

    override fun hashCode(): Int {
        return property.hashCode()
    }


}