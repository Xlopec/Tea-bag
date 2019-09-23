package com.max.weatherviewer.presentation.map.google

import com.max.weatherviewer.api.weather.Location

sealed class Message {
    data class UpdateCamera(val location: Location,
                            val zoom: Float,
                            val bearing: Float,
                            val tilt: Float) : Message()

    object Select : Message()
}

data class State(val location: Location = Location(50.431782, 30.516382),
                 val zoom: Float = 5f,
                 val bearing: Float = 0f,
                 val tilt: Float = 0f)

sealed class Command {
    data class SelectAndQuit(val location: Location) : Command()
}