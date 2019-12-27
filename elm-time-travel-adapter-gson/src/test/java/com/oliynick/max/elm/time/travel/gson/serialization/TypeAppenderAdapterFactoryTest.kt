package com.oliynick.max.elm.time.travel.gson.serialization

import com.google.gson.GsonBuilder
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private sealed class A

private sealed class B : A()

private data class C(
    val value: String = "C"
) : B()

private data class D(
    val i: Int = 10,
    val c: C = C(),
    val l: List<C?> = listOf(C(), null),
    val arrC: Array<C?> = arrayOf(C(), null),
    val nilC: C? = null
) : A() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as D

        if (i != other.i) return false
        if (c != other.c) return false
        if (l != other.l) return false
        if (!arrC.contentEquals(other.arrC)) return false
        if (nilC != other.nilC) return false

        return true
    }

    override fun hashCode(): Int {
        var result = i
        result = 31 * result + c.hashCode()
        result = 31 * result + l.hashCode()
        result = 31 * result + arrC.contentHashCode()
        result = 31 * result + (nilC?.hashCode() ?: 0)
        return result
    }
}

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private val serializer = GsonBuilder()
        .setPrettyPrinting()
        .apply {
            registerTypeAdapterFactory(TypeAppenderAdapterFactory)
        }.create()

    @Test
    fun `test NotifyComponentAttached is serializing correctly`() {

        val message = D()
        val json = serializer.toJson(message)

        println(json)

        val fromJson = serializer.fromJson(json, A::class.java)

        fromJson shouldBe message
    }

}
