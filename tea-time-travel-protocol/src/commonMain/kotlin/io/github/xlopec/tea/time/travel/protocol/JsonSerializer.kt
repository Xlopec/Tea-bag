/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.time.travel.protocol

import kotlin.reflect.KClass

/**
 * An object-to-JSON converter.
 *
 * @param J JSON tree type
 */
public interface JsonSerializer<J> {

    /**
     * Converts an object instance to a JSON tree representation.
     * The JSON representation is library-specific. For example, Gson
     * has JsonElement, whereas other libraries use [Map].
     *
     * @param any object to convert to a JSON tree
     * @param T object type
     * @return JSON tree
     */
    public fun <T> toJsonTree(
        any: T
    ): J

    /**
     * Converts a JSON tree to an object instance.
     *
     * @param T object type
     * @param json JSON tree
     * @param cl object class
     * @return parsed object instance
     */
    public fun <T : Any> fromJsonTree(
        json: J,
        cl: KClass<T>
    ): T

    /**
     * Converts an object instance to a JSON string.
     *
     * @param T object type
     * @param any object to convert to a JSON string
     * @return JSON string representation
     */
    public fun <T> toJson(
        any: T
    ): String

    /**
     * Converts a JSON string to an object instance.
     *
     * @param T object type
     * @param json JSON string
     * @param cl object class
     * @return parsed object instance
     */
    public fun <T : Any> fromJson(
        json: String,
        cl: KClass<T>
    ): T
}
