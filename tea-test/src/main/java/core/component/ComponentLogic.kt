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

package core.component

import com.oliynick.max.tea.core.component.*

inline class TodoState(val items: List<Item> = emptyList()) {
    constructor(vararg items: Item) : this(listOf(*items))
}

data class Item(val what: String) {
    init {
        require(what.isNotBlank() && what.isNotEmpty())
    }
}

sealed class Message

data class AddItem(val item: Item) : Message()

data class Updated(val items: List<Item>) : Message()

data class RemoveItem(val item: Item) : Message()

sealed class Command

data class DoAddItem(val item: Item, val to: List<Item> = emptyList()) : Command()

data class DoRemoveItem(val item: Item, val from: List<Item> = emptyList()) : Command()

suspend fun testResolver(cmd: Command): Set<Message> {
    return when (cmd) {
        is DoAddItem -> cmd.effect { Updated(to + item) }
        is DoRemoveItem -> cmd.effect { Updated(from - item) }
    }
}

fun testUpdate(message: Message, state: TodoState): UpdateWith<TodoState, Command> {
    return when (message) {
        is Updated -> TodoState(message.items).noCommand()
        is AddItem -> state command DoAddItem(message.item, state.items)
        is RemoveItem -> state command DoRemoveItem(message.item, state.items)
    }
}