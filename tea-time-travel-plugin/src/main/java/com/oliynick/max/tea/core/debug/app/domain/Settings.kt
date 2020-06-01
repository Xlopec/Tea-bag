package com.oliynick.max.tea.core.debug.app.domain

data class ServerAddress(
    val host: Host,
    val port: Port
)

class Host private constructor(
    val value: String
) {

    companion object {

        fun of(
            value: String?
        ) = value
            ?.takeUnless { host -> host.isEmpty() || host.isBlank() }
            ?.let(::Host)
    }
}

class Port private constructor(
    val value: UInt
) {

    companion object {

        fun of(
            value: UInt
        ): Port = Port(value)

        fun of(
            value: String?
        ): Port? = value?.toUIntOrNull()?.let(::of)
    }
}

//todo add remote call timeout
data class Settings(
    val host: Validated<Host>,
    val port: Validated<Port>,
    val isDetailedOutput: Boolean
) {

    companion object {

        fun of(
            hostInput: String?,
            portInput: String?,
            isDetailedOutput: Boolean
        ): Settings {

            val host = Host.of(hostInput)?.let { host -> Valid(hostInput ?: "", host) }
                ?: Invalid(hostInput ?: "", "Host can't be blank or empty")

            val port = Port.of(portInput)?.let { port -> Valid(portInput ?: "", port) }
                ?: Invalid(portInput ?: "", "Invalid port")

            return Settings(host, port, isDetailedOutput)
        }
    }
}