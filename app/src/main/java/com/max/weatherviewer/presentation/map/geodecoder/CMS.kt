package com.max.weatherviewer.presentation.map.geodecoder

import androidx.annotation.VisibleForTesting
import com.max.weatherviewer.api.weather.Location
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

sealed class Message

data class DecodeLocation(val location: Location) : Message()

data class Address(val address: String) : Message()

sealed class State

data class Preview(val address: String? = null) : State()

sealed class Command

data class DoDecodeLocation(val location: Location) : Command()

@VisibleForTesting
fun update(m: Message, s: State): UpdateWith<State, Command> {
    return when (m) {
        is DecodeLocation -> s command DoDecodeLocation(m.location)
        is Address -> Preview(m.address).noCommand()
    }
}