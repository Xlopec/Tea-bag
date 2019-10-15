/*
 * Copyright (C) 2019 Maksym Oliinyk.
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