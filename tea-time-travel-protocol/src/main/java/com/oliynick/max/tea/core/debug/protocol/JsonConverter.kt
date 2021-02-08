/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
