package com.max.weatherviewer.presentation.map

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.component.Component
import com.max.weatherviewer.defaultNavOptionsBuilder
import com.max.weatherviewer.di.fragmentScope
import com.max.weatherviewer.navigateDefaultAnimated
import com.max.weatherviewer.presentation.viewer.WeatherViewerFragmentArgs
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MapComponent = Component<Message, Command, State>

fun mapModule(fragment: Fragment, preSelectedLocation: Location?) = Kodein.Module("map") {

    bind<MapComponent>("map") with scoped(fragment.fragmentScope).singleton {
        Component(State(preSelectedLocation ?: Location(.0, .0)), ResolverImp(Navigator(fragment))::invoke, ::update)
    }
}

private class Navigator(private val fragment: Fragment) {

    private val navOptions =
        defaultNavOptionsBuilder().setLaunchSingleTop(true).setPopUpTo(R.id.mapFragment, true).build()

    fun navigateToWeatherViewer(location: Location) {
        // fixme add communication bus instead of direct args passing
        fragment.findNavController()
            .navigateDefaultAnimated(R.id.weatherViewer, WeatherViewerFragmentArgs(location).toBundle(), navOptions)
    }
}

private class ResolverImp(private val navigator: Navigator) {

    suspend fun invoke(cmd: Command): Message? {
        return when (cmd) {
            is Command.SelectAndQuit -> { navigator.navigateToWeatherViewer(cmd.location); null }
        }
    }

}

fun update(m: Message, s: State): Pair<State, Command?> {
    return when (m) {
        is Message.MoveTo -> State(m.location) to null
        Message.Select -> s to Command.SelectAndQuit(s.location)
    }
}