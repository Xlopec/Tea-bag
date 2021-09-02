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

package com.max.reader.app.exception

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.max.reader.app.env.storage.HasGson
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

sealed class AppException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(
        message: String,
        cause: Throwable,
    ) : super(message, cause)

    constructor(cause: Throwable) : super(cause)

    @RequiresApi(api = Build.VERSION_CODES.N)
    constructor(
        message: String,
        cause: Throwable,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
    ) : super(message, cause, enableSuppression, writableStackTrace)

}

class NetworkException(
    message: String,
    cause: Throwable,
) : AppException(message, cause)

class InternalException(
    message: String,
    cause: Throwable,
) : AppException(message, cause)

suspend fun <Env> Env.toAppException(
    th: Throwable
): AppException where Env : HasGson =
    th.wrap { raw ->
        when (raw) {
            is IOException -> NetworkException(raw)
            is ClientRequestException -> toAppException(raw)
            else -> null
        }
    } ?: InternalException("An internal exception occurred", th)

private suspend inline fun Throwable.wrap(
    crossinline transform: suspend (Throwable) -> AppException?,
): AppException? =
    if (this is AppException) this
    else transform(this) ?: cause?.let { th -> transform(th) }

private suspend fun <Env> Env.toAppException(
    exception: ClientRequestException,
): AppException where Env : HasGson =
    NetworkException(
        gson.readErrorMessage(exception) ?: exception.toGenericExceptionDescription(),
        exception
    )

private suspend fun Gson.readErrorMessage(
    exception: ClientRequestException,
) = withContext(Dispatchers.IO) {
    fromJson(exception.response.readText(), JsonObject::class.java)["message"]
        ?.takeUnless { elem -> elem.isJsonNull }
        ?.asString
}

private fun ClientRequestException.toGenericExceptionDescription() =
    "Server returned status code ${response.status.value}"

private fun NetworkException(
    cause: IOException,
) = NetworkException("Network exception occurred, check connectivity", cause)
