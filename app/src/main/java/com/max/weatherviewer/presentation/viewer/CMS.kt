package com.max.weatherviewer.presentation.viewer

import androidx.annotation.VisibleForTesting
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather
import com.oliynick.max.elm.core.component.UpdateWith
import com.oliynick.max.elm.core.component.command
import com.oliynick.max.elm.core.component.noCommand

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

@VisibleForTesting
fun update(message: Message, s: State): UpdateWith<State, Command> {
    return when (message) {
        is Message.LocationQueried -> State.Loading(s.location) command Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(s.location, message.weather).noCommand()
        is Message.OpFuckup -> State.LoadFailure(s.location, message.th).noCommand()
        Message.SelectLocation -> s command Command.SelectLocation(s.location)
        Message.Retry -> calculateRetryAction(s)
    }
}

private fun calculateRetryAction(current: State): UpdateWith<State, Command> {
    return when (current) {
        is State.LoadFailure -> State.Loading(current.location) command Command.LoadWeather(current.location)
        is State.Loading, is State.Preview -> throw IllegalStateException("Shouldn't get there, was $current")
    }
}