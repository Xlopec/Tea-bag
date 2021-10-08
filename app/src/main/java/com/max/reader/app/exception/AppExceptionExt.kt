package com.max.reader.app.exception

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.max.reader.app.AppException
import com.max.reader.app.InternalException
import com.max.reader.app.env.storage.HasGson
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

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
    com.max.reader.app.NetworkException(
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
) = com.max.reader.app.NetworkException("Network exception occurred, check connectivity", cause)