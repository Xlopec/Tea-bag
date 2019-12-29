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

package protocol

import com.google.gson.JsonElement
import java.util.*

typealias Json = String
typealias JsonTree = JsonElement

// todo implement later
enum class ServerMessageType {
    ComponentSnapshot,
    ComponentAttached,
    ActionApplied
}


sealed class ServerMessage

data class NotifyComponentSnapshot(
    val message: JsonElement,
    val oldState: JsonElement,
    val newState: JsonElement
) : ServerMessage()

data class NotifyComponentAttached(
    val state: JsonElement
) : ServerMessage()

data class ActionApplied(
    val id: UUID
) : ServerMessage()

/*
fun ComponentSnapshot(
    message: JsonTree,
    oldState: JsonTree,
    newState: JsonTree
) = JsonObject {
    addProperty("@type", ServerMessageType.ComponentSnapshot.toString())
    add("@message", message)
    add("@oldState", oldState)
    add("@newState", newState)
}

fun ComponentAttached(
    state: JsonTree
) = JsonObject {
    addProperty("@type", ServerMessageType.ComponentAttached.toString())
    add("@state", state)
}

fun ActionApplied(
    id: UUID
) = JsonObject {
    addProperty("@type", ServerMessageType.ActionApplied.toString())
    addProperty("@id", id.toString())
}
*/
