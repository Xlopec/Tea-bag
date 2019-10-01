package com.oliynick.max.elm.time.travel.protocol

import com.google.gson.annotations.SerializedName
import java.util.*

sealed class Action

data class ApplyCommands(val commands: List<Any>) : Action() {

    constructor(command: Any) : this(listOf(command))

    init {
        require(commands.isNotEmpty())
    }
}

data class Packet(
    @SerializedName("id") val id: UUID,
    @SerializedName("component") val component: String,
    @SerializedName("action") val action: Action
)