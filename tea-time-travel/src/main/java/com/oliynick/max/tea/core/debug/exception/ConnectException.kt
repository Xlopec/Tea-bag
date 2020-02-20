package com.oliynick.max.tea.core.debug.exception

class ConnectException(
    message: String,
    cause: Throwable
) : RuntimeException(message, cause)
