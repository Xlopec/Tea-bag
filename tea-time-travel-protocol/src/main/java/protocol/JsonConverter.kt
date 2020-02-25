package protocol

interface JsonConverter<J> {

    fun <T> toJsonTree(
        any: T
    ): J

    fun <T> fromJsonTree(
        json: J,
        cl: Class<T>
    ): T

    fun <T> toJson(
        any: T
    ): String

    fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T
}