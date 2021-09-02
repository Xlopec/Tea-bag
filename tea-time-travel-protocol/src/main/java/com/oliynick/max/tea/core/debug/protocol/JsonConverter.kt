/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.oliynick.max.tea.core.debug.protocol

/**
 * Object to json converter
 *
 * @param J json tree
 */
public interface JsonConverter<J> {

    /**
     * Converts object instance to a json tree representation.
     * Json representation is library specific. For example, Gson
     * has JsonElement whereas other libraries use [Map]
     *
     * @param any object to convert to json tree
     * @param T object type
     * @return json tree
     */
    public fun <T> toJsonTree(
        any: T
    ): J

    /**
     * Converts json tree to object instance
     *
     * @param json json tree
     * @param cl object class
     * @param T object type
     * @return parsed object instance
     */
    public fun <T> fromJsonTree(
        json: J,
        cl: Class<T>
    ): T

    /**
     * Converts object instance to a json string
     *
     * @param any object to convert to json string
     * @param T object type
     * @return parsed object instance
     */
    public fun <T> toJson(
        any: T
    ): String

    /**
     * Converts json tree to object instance
     *
     * @param json json string
     * @param cl object class
     * @param T object type
     * @return parsed object instance
     */
    public fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T
}
