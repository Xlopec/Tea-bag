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

package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import kotlin.contracts.contract

sealed interface State {
    val settings: Settings
}

fun State.updateSettings(
    how: Settings.() -> Settings
) = when (this) {
    is Stopped -> copy(settings = settings.run(how))
    is Starting -> copy(settings = settings.run(how))
    is Started -> copy(settings = settings.run(how))
    is Stopping -> copy(settings = settings.run(how))
}

fun State.updateServerSettings(
    settings: Settings
) =
    when (this) {
        is Stopped -> copy(settings = settings)
        is Starting -> copy(settings = settings)
        is Started -> copy(settings = settings)
        is Stopping -> copy(settings = settings)
    }

fun State.canExport(): Boolean {
    contract {
        returns(true) implies (this@canExport is Started)
    }

    return isStarted() && debugState.components.isNotEmpty()
}

fun State.canStart(): Boolean {
    contract {
        returns(true) implies (this@canStart is Stopped)
    }
    return this is Stopped && canStart
}

fun State.canImport(): Boolean {
    contract {
        returns(true) implies (this@canImport is Started)
    }

    return isStarted()
}

fun State.isStarted(): Boolean {
    contract {
        returns(true) implies (this@isStarted is Started)
    }
    return this is Started
}
