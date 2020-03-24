package com.oliynick.max.tea.core.debug.app.domain.cms

import kotlinx.coroutines.TimeoutCancellationException
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.*
import javax.net.ssl.SSLException

sealed class PluginException : Throwable {
    constructor(
        message: String,
        cause: Throwable
    ) : super(message, cause)

    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(
        message: String,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}

class UserException(
    message: String,
    cause: Throwable
) : PluginException(message, cause)

class NetworkException(
    message: String,
    cause: Throwable
) : PluginException(message, cause)

class MissingDependenciesException(
    message: String,
    cause: Throwable
) : PluginException(message, cause)

class InternalException(
    message: String,
    cause: Throwable
) : PluginException(message, cause)

fun Throwable.toPluginException(): PluginException {
    if (this is PluginException) {
        return this
    }

    return when {
        isMissingDependenciesException -> MissingDependenciesException(
            message!!,
            this)
        isNetworkException -> NetworkException(message ?: "Network exception occurred",
                                               this)
        else -> InternalException(message ?: "Internal exception occurred", this)
    }
}

private inline fun Throwable.findCause(crossinline predicate: (Throwable) -> Boolean): Throwable? {
    var cause: Throwable? = this

    while (cause != null) {
        if (predicate(cause)) {
            return cause
        }

        cause = cause.cause
    }

    return null
}

private val Throwable.isMissingDependenciesException
    get() = findCause { it is ClassNotFoundException } != null

private val Throwable.isNetworkException
    // some of IO exceptions
    get() = findCause {
        it is TimeoutException
            || it is TimeoutCancellationException
            || it is UnknownHostException
            || it is SSLException
            || it is SocketTimeoutException
            || it is ProtocolException
    } != null