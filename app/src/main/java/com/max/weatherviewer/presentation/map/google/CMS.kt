package com.max.weatherviewer.presentation.map.google

import androidx.annotation.VisibleForTesting
import com.max.weatherviewer.api.weather.Location
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

sealed class Message {
    data class UpdateCamera(val location: Location,
                            val zoom: Float,
                            val bearing: Float,
                            val tilt: Float) : Message()

    object Select : Message()
}

data class State(val location: Location,
                 val zoom: Float,
                 val bearing: Float,
                 val tilt: Float)

/*data class State(val location: Location = Location(50.431782, 30.516382),
                 val zoom: Float = 5f,
                 val bearing: Float = 0f,
                 val tilt: Float = 0f)*/

sealed class Command {
    data class SelectAndQuit(val location: Location) : Command()
}

@VisibleForTesting
fun update(m: Message, s: State): UpdateWith<State, Command> {
    return when (m) {
        is Message.UpdateCamera -> State(m.location, m.zoom, m.bearing, m.tilt).noCommand()
        Message.Select -> s command Command.SelectAndQuit(s.location)
    }
}