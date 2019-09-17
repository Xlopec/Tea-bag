package com.max.weatherviewer.presentation.map.google

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.defaultNavOptionsBuilder
import com.max.weatherviewer.di.fragmentScope
import com.max.weatherviewer.navigateDefaultAnimated
import com.max.weatherviewer.presentation.viewer.WeatherViewerFragmentArgs
import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.flow.Flow
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MapComponent = (Flow<Message>) -> Flow<State>

fun mapModule(fragment: Fragment, preSelectedLocation: Location?) = Kodein.Module("map") {

    bind<Dependencies>() with scoped(fragment.fragmentScope).singleton { Dependencies(fragment) }

    bind<MapComponent>("map") with scoped(fragment.fragmentScope).singleton {

        suspend fun resolve(command: Command) = instance<Dependencies>().resolve(command)

        androidLogger(component(State(
            preSelectedLocation ?: Location(.0, .0)), ::resolve, ::update), "Map")
    }
}

@VisibleForTesting
fun update(m: Message, s: State): UpdateWith<State, Command> {
    return when (m) {
        is Message.MoveTo -> State(m.location).noCommand()
        Message.Select -> s command Command.SelectAndQuit(s.location)
    }
}

private data class Dependencies(val fragment: Fragment)

private val navOptions: NavOptions =
    defaultNavOptionsBuilder().setLaunchSingleTop(true).setPopUpTo(R.id.mapFragment, true).build()

private suspend fun Dependencies.resolve(command: Command): Set<Message> {
    return when (command) {
        is Command.SelectAndQuit -> command.sideEffect { fragment.navigateToWeatherViewer(location) }
    }
}

private fun Fragment.navigateToWeatherViewer(location: Location) {
    findNavController()
        .navigateDefaultAnimated(R.id.weatherViewer, WeatherViewerFragmentArgs(location).toBundle(),
                                 navOptions)
}