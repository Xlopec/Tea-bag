package com.oliynick.max.elm.time.travel.protocol

sealed class Action

data class ApplyCommands(val commands: List<Any>) : Action() {

    constructor(command: Any) : this(listOf(command))

    init {
        require(commands.isNotEmpty())
    }
}
