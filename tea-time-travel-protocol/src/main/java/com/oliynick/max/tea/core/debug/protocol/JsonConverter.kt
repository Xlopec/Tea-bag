package com.oliynick.max.tea.core.debug.protocol

/**
 * Object to json converter
 *
 * @param J json tree
 */
interface JsonConverter<J> {

    /**
     * Converts object instance to a json tree representation.
     * Json representation is library specific. For example, Gson
     * has JsonElement whereas other libraries use [Map]
     *
     * @param any object to convert to json tree
     * @param T object type
     * @return json tree
     */
    fun <T> toJsonTree(
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
    fun <T> fromJsonTree(
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
    fun <T> toJson(
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
    fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T
}