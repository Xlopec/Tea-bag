package com.max.weatherviewer.app.exception

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonObject
import com.max.weatherviewer.app.env.storage.HasGson
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
