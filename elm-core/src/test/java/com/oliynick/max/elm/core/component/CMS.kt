package com.oliynick.max.elm.core.component

inline class TodoState(val items: List<Item> = emptyList())

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

data class DoAddItem(val item: Item, val from: List<Item>) : Command()

data class DoRemoveItem(val item: Item, val from: List<Item>) : Command()