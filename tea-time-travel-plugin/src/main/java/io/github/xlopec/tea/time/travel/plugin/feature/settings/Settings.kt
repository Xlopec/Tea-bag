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

@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.settings

import arrow.core.Validated
import io.github.xlopec.tea.time.travel.plugin.model.Input
import io.github.xlopec.tea.time.travel.plugin.model.PositiveNumber
import io.github.xlopec.tea.time.travel.plugin.model.toPositive

data class ServerAddress(
    val host: Host,
    val port: Port
)

class Host private constructor(
    val value: String
) {

    companion object {

        fun newOrNull(
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
    val host: Input<String, Host>,
    val port: Input<String, Port>,
    val isDetailedOutput: Boolean,
    val clearSnapshotsOnAttach: Boolean,
    val maxSnapshots: PositiveNumber = DefaultMaxSnapshots,
) {

    companion object {

        val DefaultMaxSnapshots = PositiveNumber.of(200U)

        fun fromInput(
            hostInput: String?,
            portInput: String?,
            isDetailedOutput: Boolean,
            clearLogsOnComponentAttach: Boolean,
            maxSnapshots: UInt,
        ) = Settings(
            ValidatedHost(hostInput),
            ValidatedPort(portInput),
            isDetailedOutput,
            clearLogsOnComponentAttach,
            maxSnapshots.toPositive()
        )
    }
}

fun ValidatedHost(
    input: String?
) = Input(input ?: "", Validated.fromNullable(Host.newOrNull(input)) { "Host can't be blank or empty" })

fun ValidatedPort(
    input: String?
) = Input(input ?: "", Validated.fromNullable(input?.toIntOrNull()?.let(::Port)) { "Invalid port" })
