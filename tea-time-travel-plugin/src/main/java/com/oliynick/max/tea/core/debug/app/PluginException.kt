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

package com.oliynick.max.tea.core.debug.app

import kotlinx.coroutines.TimeoutCancellationException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException
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
        isMissingDependenciesException -> MissingDependenciesException(message!!, this)
        isNetworkException -> NetworkException(message ?: "Network exception occurred", this)
        else -> InternalException(message ?: "Internal exception occurred", this)
    }
}

private inline fun Throwable.findCause(
    crossinline predicate: (Throwable) -> Boolean
): Throwable? {
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
    get() = findCause { th ->
        th is TimeoutException
                || th is TimeoutCancellationException
                || th is UnknownHostException
                || th is SSLException
                || th is SocketTimeoutException
                || th is ProtocolException
                || th is UnresolvedAddressException
                || th is SocketException
    } != null
