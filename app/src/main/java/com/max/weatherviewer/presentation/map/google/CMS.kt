package com.max.weatherviewer.presentation.map.google

import com.max.weatherviewer.api.weather.Location

sealed class Message {
    data class MoveTo(val location: Location) : Message()
    object Select : Message()
}

data class State(val location: Location)

sealed class Command {
    data class SelectAndQuit(val location: Location) : Command()
}

@Suppress("FunctionName")
fun StateOf(location: Location?) = State(location ?: Location(.0, .0))