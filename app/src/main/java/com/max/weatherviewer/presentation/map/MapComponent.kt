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
import io.reactivex.Completable
import io.reactivex.Single
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MapComponent = Component<Message, Command, State>

fun mapModule(fragment: Fragment, preSelectedLocation: Location?) = Kodein.Module("map") {

    bind<MapComponent>("map") with scoped(fragment.fragmentScope).singleton {
        Component(State(preSelectedLocation ?: Location(.0, .0)), ResolverImp(Navigator(fragment)), ::update)
    }
}

private class Navigator(private val fragment: Fragment) {

    private val navOptions =
        defaultNavOptionsBuilder().setLaunchSingleTop(true).setPopUpTo(R.id.mapFragment, true).build()

    fun navigateToWeatherViewer(location: Location) {
        // fixme add communication bus instead of direct args passing
        fragment.findNavController()
            .navigateDefaultAnimated(R.id.weatherViewer, toNavArgs(location).toBundle(), navOptions)
    }

    private fun toNavArgs(location: Location): WeatherViewerFragmentArgs {
        return WeatherViewerFragmentArgs.Builder(location).build()
    }
}

private class ResolverImp(private val navigator: Navigator) : (Command) -> Single<out Message> {

    override fun invoke(cmd: Command): Single<out Message> {
        return when (cmd) {
            Command.None -> Single.never()
            is Command.SelectAndQuit ->
                Completable.fromAction { navigator.navigateToWeatherViewer(cmd.location) }.andThen(Single.never())
        }
    }

}

fun update(m: Message, s: State): Pair<State, Command> {
    return when (m) {
        is Message.MoveTo -> State(m.location) to Command.None
        Message.Select -> s to Command.SelectAndQuit(s.location)
    }
}