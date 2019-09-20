package com.max.weatherviewer.presentation.viewer

import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather

sealed class State {

    data class Loading(override val location: Location) : State()

    data class Preview(override val location: Location, val data: Weather) : State()

    data class LoadFailure(override val location: Location, val th: Throwable) : State()

    abstract val location: Location

}

sealed class Message {

    object SelectLocation : Message()

    data class LocationQueried(val l: Location) : Message()

    data class WeatherLoaded(val weather: Weather) : Message()

    data class OpFuckup(val th: Throwable) : Message()

    object Retry : Message()
}

sealed class Command {

    data class LoadWeather(val l: Location) : Command()

    data class FeedLoaded(val data: Weather) : Command()

    data class SelectLocation(val withSelectedLocation: Location?) : Command()
}