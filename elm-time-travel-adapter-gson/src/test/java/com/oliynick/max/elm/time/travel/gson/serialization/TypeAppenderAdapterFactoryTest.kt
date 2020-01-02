package com.oliynick.max.elm.time.travel.gson.serialization

import com.google.gson.GsonBuilder
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private val serializer = GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .apply {
            registerTypeAdapterFactory(TypeAppenderAdapterFactory)
        }.create()

    @Test
    fun `test class hierarchy gets serialized correctly`() {

        val message = D()
        val json = serializer.toJson(message)

        println(json)

        val fromJson = serializer.fromJson(json, A::class.java)

        fromJson shouldBe message
    }

}
