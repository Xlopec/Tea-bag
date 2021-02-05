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

@file:Suppress("unused")

package com.oliynick.max.tea.core.debug.protocol

import java.util.*

/**
 * Message that notifies a debug server about component's state changes
 *
 * @param messageId message identifier
 * @param componentId component identifier
 * @param payload payload that tells a debug server how to process this message
 * @param J json specific implementation
 */
data class NotifyServer<out J>(
    val messageId: UUID,
    val componentId: ComponentId,
    val payload: ServerMessage<J>,
)

/**
 * Message that tells a client to apply changes
 *
 * @param id message identifier
 * @param component component identifier
 * @param message payload that tells client how to process this message
 * @param J json specific implementation
 */
data class NotifyClient<out J>(
    val id: UUID,
    val component: ComponentId,
    val message: ClientMessage<J>,
)

