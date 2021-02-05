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

package com.oliynick.max.tea.core.debug.protocol

/**
 * Represents messages that should be consumed by debug server
 *
 * @param J implementation specific json type
 */
sealed class ServerMessage<out J>

/**
 * Represents message that informs server about state changes
 *
 * @param message message that was applied to debug component
 * @param oldState old component's state
 * @param newState new component's state
 * @param J implementation specific json type
 */
data class NotifyComponentSnapshot<out J>(
    val message: J,
    val oldState: J,
    val newState: J,
) : ServerMessage<J>()

/**
 * Notifies that new component attached to debug server
 *
 * @param state current component's state
 * @param J implementation specific json type
 */
data class NotifyComponentAttached<out J>(
    val state: J,
) : ServerMessage<J>()
