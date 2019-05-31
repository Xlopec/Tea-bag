package com.max.weatherviewer.presentation.start

import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather

sealed class State {

    object Loading : State()

    data class Preview(val data: Weather? = null) : State() {
        companion object {
            private val EMPTY = Preview()
            fun empty() = EMPTY
        }
    }

    data class Failure(val th: Throwable) : State()

    object PermissionRequestFuckup : State()

    object ShowPermissionRationale : State()

    object RequestPermission : State()

}

sealed class Message {

    object LoadButtonClicked : Message()

    data class LocationQueried(val l: Location) : Message()

    data class WeatherLoaded(val weather: Weather) : Message()

    data class LoadFuckup(val th: Throwable) : Message()

    object PermissionFuckup : Message()

    object RequestPermission : Message()

    object ShowPermissionRationale : Message()
}

sealed class Command {

    object None : Command()

    data class LoadWeather(val l: Location) : Command()

    data class FeedLoaded(val data: Weather) : Command()

    data class FeedLoadFailure(val th: Throwable) : Command()

    object PermissionRequestFuckup : Command()

    object ShowPermissionRationale : Command()

    object QueryLocation : Command()
}