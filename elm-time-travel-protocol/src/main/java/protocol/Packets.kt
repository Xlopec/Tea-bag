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

package protocol

import java.util.*

data class SomeTestString(val value: String)

data class SomeTestCommand constructor(
    val str: SomeTestString,
    val collection: Collection<Any>
)

data class SomeTestState(val string: SomeTestString/*, val uri: Uri*/)

data class NotifyServer(
    val messageId: UUID,
    val componentId: ComponentId,
    val payload: ServerMessage
)

data class NotifyClient(
    val id: UUID,
    val component: ComponentId,
    val message: ClientMessage
)

