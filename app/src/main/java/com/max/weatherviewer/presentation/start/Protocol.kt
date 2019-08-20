package com.max.weatherviewer.presentation.start

import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather

sealed class State {

    data class Loading(override val location: Location) : State()

    data class Preview(override val location: Location, val data: Weather) : State()

    data class Initial(override val location: Location) : State()

    data class LoadFailure(override val location: Location, val th: Throwable) : State()

    data class PermissionRequestFuckup(override val location: Location) : State()

    data class ShowPermissionRationale(override val location: Location) : State()

    data class RequestPermission(override val location: Location) : State()

    abstract val location: Location

}

sealed class Message {

    object ViewAttached : Message()

    object SelectLocation : Message()

    object LoadButtonClicked : Message()

    data class LocationQueried(val l: Location) : Message()

    data class WeatherLoaded(val weather: Weather) : Message()

    data class OpFuckup(val th: Throwable) : Message()

    object PermissionFuckup : Message()

    object RequestPermission : Message()

    object ShowPermissionRationale : Message()

    object Retry : Message()
}

sealed class Command {

    object None : Command()

    data class LoadWeather(val l: Location) : Command()

    data class FeedLoaded(val data: Weather) : Command()

    object PermissionRequestFuckup : Command()

    object ShowPermissionRationale : Command()

    object QueryLocation : Command()

    data class SelectLocation(val withSelectedLocation: Location?) : Command()
}