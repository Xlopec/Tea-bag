package com.max.weatherviewer.presentation.map.google

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.defaultNavOptionsBuilder
import com.max.weatherviewer.di.fragmentScope
import com.max.weatherviewer.navigateDefaultAnimated
import com.max.weatherviewer.persistence.load
import com.max.weatherviewer.persistence.persist
import com.max.weatherviewer.presentation.viewer.WeatherViewerFragmentArgs
import com.oliynick.max.elm.core.component.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MapComponent = (Flow<Message>) -> Flow<State>

fun <S> S.mapModule(preSelectedLocation: Location?): Kodein.Module where S : Fragment,
                                                                         S : CoroutineScope {
    return Kodein.Module("map") {

        bind<Gson>() with singleton { Gson() }

        bind<Dependencies>() with scoped(fragmentScope).singleton { Dependencies(this@mapModule) }

        bind<Interceptor<Message, State, Command>>() with singleton {
            androidLogger<Message, Command, State>("map") with { _, _, new, _ -> instance<Context>().persist(instance(), new) }
        }

        bind<MapComponent>("map") with scoped(fragmentScope).singleton {

            suspend fun resolve(command: Command) = instance<Dependencies>().resolve(command)

            suspend fun loader() = (preSelectedLocation?.let(::State) ?: instance<Context>().load(instance(), ::State)) to emptySet<Command>()

            component(::loader, ::resolve, ::update, instance())
        }
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