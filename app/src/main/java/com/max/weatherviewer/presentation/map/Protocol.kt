package com.max.weatherviewer.presentation.map

import com.max.weatherviewer.api.weather.Location

sealed class Message {
    data class MoveTo(val location: Location) : Message()
    object Select : Message()
}

data class State(val location: Location = Location(.0, .0))

sealed class Command {
    object None : Command()
    data class SelectAndQuit(val location: Location) : Command()
}