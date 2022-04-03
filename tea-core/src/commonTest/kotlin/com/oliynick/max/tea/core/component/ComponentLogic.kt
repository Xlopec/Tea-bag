/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oliynick.max.tea.core.component

import kotlin.jvm.JvmInline

@JvmInline
value class TodoState(val items: List<Item> = emptyList()) {
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
