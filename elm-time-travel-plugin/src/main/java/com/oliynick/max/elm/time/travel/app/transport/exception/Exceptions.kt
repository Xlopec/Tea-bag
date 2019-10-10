package com.oliynick.max.elm.time.travel.app.transport.exception

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.StatusPages

fun Application.installErrorInterceptors() {
    install(StatusPages) {

        exception<RuntimeException> { cause ->
         //   call.respond(HttpStatusCode.InternalServerError, ErrorResponse(cause))
            throw cause
        }
    }
}

inline fun Throwable.findCause(crossinline predicate: (Throwable) -> Boolean): Throwable? {
    var cause: Throwable? = this

    while (cause != null) {
        if (predicate(cause)) {
            return cause
        }

        cause = cause.cause
    }

    return null
}