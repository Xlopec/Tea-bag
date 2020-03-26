package com.oliynick.max.tea.core.debug.app.domain

const val defaultHost = "0.0.0.0"
const val defaultPort = 8080U

//todo add remote call timeout
data class ServerSettings(
    val host: String,
    val port: UInt
)

data class Settings(
    val serverSettings: ServerSettings
)