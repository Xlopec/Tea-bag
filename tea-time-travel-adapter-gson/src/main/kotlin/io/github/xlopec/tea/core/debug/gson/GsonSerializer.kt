/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.github.xlopec.tea.core.debug.protocol.JsonSerializer
import kotlin.reflect.KClass

/**
 * Configures and creates a new [converter][GsonSerializer] instance
 */
public fun GsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonSerializer<JsonElement> = GsonSerializer(Gson(config))

private class GsonSerializer(
    private val gson: Gson
) : JsonSerializer<JsonElement> {

    override fun <T> toJsonTree(
        any: T
    ): JsonElement = gson.toJsonTree(any)

    override fun <T : Any> fromJsonTree(
        json: JsonElement,
        cl: KClass<T>
    ): T = gson.fromJson(json, cl.java)

    override fun <T> toJson(
        any: T
    ): String = gson.toJson(any)

    override fun <T : Any> fromJson(
        json: String,
        cl: KClass<T>
    ): T = gson.fromJson(json, cl.java)

}
