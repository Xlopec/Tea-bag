package com.oliynick.max.elm.time.travel.app.exception

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