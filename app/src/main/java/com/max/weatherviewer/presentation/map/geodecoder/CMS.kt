package com.max.weatherviewer.presentation.map.geodecoder

import com.max.weatherviewer.api.weather.Location

sealed class Message
data class DecodeQuery(val query: String = "") : Message()
data class DecodeLocation(val location: Location) : Message()
data class Address(val address: String) : Message()
sealed class State
data class Preview(val address: String? = null) : State()
sealed class Command
data class DoDecodeQuery(val query: String = "") : Command()
data class DoDecodeLocation(val location: Location) : Command()