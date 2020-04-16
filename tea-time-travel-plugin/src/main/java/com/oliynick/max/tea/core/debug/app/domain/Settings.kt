package com.oliynick.max.tea.core.debug.app.domain

const val DEFAULT_HOST = "127.0.0.1"
const val DEFAULT_PORT = 8080U

//todo add remote call timeout
data class ServerSettings(
    val host: String,
    val port: UInt
)

data class Settings(
    val serverSettings: ServerSettings,
    val isDetailedOutput: Boolean
)