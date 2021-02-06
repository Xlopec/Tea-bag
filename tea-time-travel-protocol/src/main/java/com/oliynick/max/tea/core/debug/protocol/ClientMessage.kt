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

package com.oliynick.max.tea.core.debug.protocol

/**
 * Represents messages that should be applied by client
 *
 * @param J implementation specific json type
 */
sealed class ClientMessage<out J>

/**
 * Represents commands that tells client to apply debug [message] as if it was just
 * a regular message
 *
 * @param message message to consume
 * @param J implementation specific json type
 */
data class ApplyMessage<out J>(
    val message: J,
) : ClientMessage<J>()

/**
 * Represents commands that tells client to apply debug [state] as if it was just
 * a regular state
 *
 * @param state new application state
 * @param J implementation specific json type
 */
data class ApplyState<J>(
    val state: J,
) : ClientMessage<J>()
