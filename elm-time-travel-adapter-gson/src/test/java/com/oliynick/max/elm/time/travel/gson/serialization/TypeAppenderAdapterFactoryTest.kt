package com.oliynick.max.elm.time.travel.gson.serialization

import com.google.gson.GsonBuilder
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
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
    val l: List<C> = listOf(C())
) : A()

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private val serializer = GsonBuilder().apply {
        registerTypeAdapterFactory(TypeAppenderAdapterFactory())
    }.create()

    @Test
    fun `test NotifyComponentAttached is serializing correctly`() {

        val message = D()
        val json = serializer.toJson(message)

        println(json)

        val fromJson = serializer.fromJson(json, A::class.java)
        // gson creates such instances via unsafe, thus no singleton here
        fromJson should beInstanceOf<A>()
    }

    /*@Test
    fun `test NotifyComponentAttached is serializing correctly`() {

        val message = NotifyComponentAttached(testState)
        val json = serializer.toJson(message, ServerMessage::class.java)
        val fromJson = serializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ActionApplied is serializing correctly`() {

        val message = ActionApplied(UUID.randomUUID())
        val json = serializer.toJson(message, ServerMessage::class.java)
        val fromJson = serializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test NotifyComponentSnapshot is serializing correctly`() {

        val message = NotifyComponentSnapshot(
            "Message",
            testState,
            loadingScreenState
        )

        val json = serializer.toJson(message, ServerMessage::class.java)
        val fromJson = serializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }*/

}
