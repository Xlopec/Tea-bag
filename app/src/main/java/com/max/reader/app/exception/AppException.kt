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

package com.max.reader.app.exception

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.max.reader.app.env.storage.HasGson
import retrofit2.HttpException
import java.io.IOException

sealed class AppException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(
        message: String,
        cause: Throwable
    ) : super(message, cause)

    constructor(cause: Throwable) : super(cause)

    @RequiresApi(api = Build.VERSION_CODES.N)
    constructor(
        message: String,
        cause: Throwable,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)


}

class NetworkException(
    message: String,
    cause: Throwable
) : AppException(message, cause)

class InternalException(
    message: String,
    cause: Throwable
) : AppException(message, cause)

fun <Env> Env.toAppException(th: Throwable): AppException where Env : HasGson =
    th.wrap { raw ->
        when (raw) {
            is IOException -> NetworkException("Network exception occurred, check connectivity", raw)
            is HttpException -> toAppException(raw)
            else -> null
        }
    } ?: InternalException("An internal exception occurred", th)

private inline fun Throwable.wrap(
    crossinline transform: (Throwable) -> AppException?
): AppException? = if (this is AppException) this else transform(this) ?: cause?.let(transform)

private fun <Env> Env.toAppException(
    httpException: HttpException
): AppException where Env : HasGson {

    val message = httpException
        .response()
        ?.errorBody()
        ?.let { body -> gson.fromJson(body.charStream(), JsonObject::class.java) }
        ?.let { errObj -> errObj["message"] }
        ?.takeUnless { elem -> elem.isJsonNull }
        ?.asString

    return NetworkException(message ?: "Server returned: ${httpException.message()}", httpException)
}
