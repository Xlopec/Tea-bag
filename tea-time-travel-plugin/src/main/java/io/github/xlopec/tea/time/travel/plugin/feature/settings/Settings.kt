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

package io.github.xlopec.tea.time.travel.plugin.feature.settings

import io.github.xlopec.tea.time.travel.plugin.model.Invalid
import io.github.xlopec.tea.time.travel.plugin.model.Valid
import io.github.xlopec.tea.time.travel.plugin.model.Validated

data class ServerAddress(
    val host: Host,
    val port: Port
)

class Host private constructor(
    val value: String
) {

    companion object {

        fun of(
            value: String?
        ) = value
            ?.takeUnless { host -> host.isEmpty() || host.isBlank() }
            ?.let(::Host)
    }
}

@JvmInline
value class Port(
    val value: Int
) {
    init {
        require(value > 0)
    }
}

// todo add remote call timeout
data class Settings(
    val host: Validated<Host>,
    val port: Validated<Port>,
    val isDetailedOutput: Boolean,
    val clearSnapshotsOnAttach: Boolean,
) {

    companion object {

        fun of(
            hostInput: String?,
            portInput: String?,
            isDetailedOutput: Boolean,
            clearLogsOnComponentAttach: Boolean
        ): Settings {

            val host = Host.of(hostInput)?.let { host -> Valid(hostInput ?: "", host) }
                ?: Invalid(hostInput ?: "", "Host can't be blank or empty")

            val port = portInput?.toIntOrNull()?.let(::Port)?.let { port -> Valid(portInput, port) }
                ?: Invalid(portInput ?: "", "Invalid port")

            return Settings(host, port, isDetailedOutput, clearLogsOnComponentAttach)
        }
    }
}
