package com.max.weatherviewer.presentation.map.geodecoder

import com.max.weatherviewer.api.weather.Location

sealed class Message

data class DecodeLocation(val location: Location) : Message()

data class Address(val address: String) : Message()

sealed class State

data class Preview(val address: String? = null) : State()

sealed class Command

data class DoDecodeLocation(val location: Location) : Command()