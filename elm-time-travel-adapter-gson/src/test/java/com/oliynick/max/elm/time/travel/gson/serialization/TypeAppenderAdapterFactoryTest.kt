package com.oliynick.max.elm.time.travel.gson.serialization

import com.oliynick.max.elm.time.travel.gson.Gson
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private val serializer = Gson()

    @Test
    fun `test class hierarchy gets serialized correctly`() {

        val message = D()
        val json = serializer.toJson(message)

        val fromJson = serializer.fromJson(json, A::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test simple string gets serialized correctly`() {

        val message = "a"
        val json = serializer.toJson(message)
        val fromJson = serializer.fromJson(json, String::class.java)

        fromJson shouldBe message
    }

}
