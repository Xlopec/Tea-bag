package io.github.xlopec.tea.time.travel.gson.serialization.test

import com.google.gson.GsonBuilder
import com.google.gson.internal.`$Gson$Types`
import com.google.gson.reflect.TypeToken
import io.github.xlopec.tea.time.travel.gson.metadata.fromJsonTypeTree
import io.github.xlopec.tea.time.travel.gson.metadata.toJsonTypeTree
import org.junit.Test
import java.lang.reflect.ParameterizedType
import kotlin.test.assertTrue

class TypeSerializersTest {

    private class Box<T, out J>(
        val t: T,
        val j: J,
    )

    private inner class InnerTests

    @Test
    fun `check generic type serialized properly`() {

        val b = Box(t = "Max", j = 1)
        val token = TypeToken.get(b::class.java)

        println(token.type.typeName)

        val token2 = object : TypeToken<Box<String, Int>>() {}

        println(token2.type.typeName)


        val token3 = object : TypeToken<Box<String, InnerTests>>() {}

        println(token3.type.typeName)

        val token4 = object : TypeToken<Box<String, Array<Int>>>() {}

        println(token4.type.typeName)

        val l = (token4.type as ParameterizedType).actualTypeArguments.map { it }
        println(l)
        println((token4.type as ParameterizedType).ownerType)

        val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()

        val type4JsonTree = token4.type.toJsonTypeTree()

        println(gson.toJson(type4JsonTree))
        println()

        val type4FromJsonTree = type4JsonTree.fromJsonTypeTree()

        assertTrue(`$Gson$Types`.equals(type4FromJsonTree, token4.type))
    }

}