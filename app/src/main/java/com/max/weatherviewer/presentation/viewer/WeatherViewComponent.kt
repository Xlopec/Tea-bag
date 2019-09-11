package com.max.weatherviewer.presentation.viewer

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.max.weatherviewer.R
import com.max.weatherviewer.api.location.LocationModel
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.WeatherProvider
import com.max.weatherviewer.component.*
import com.max.weatherviewer.navigateDefaultAnimated
import com.max.weatherviewer.presentation.map.MapFragmentArgs
import kotlinx.coroutines.flow.Flow
import org.kodein.di.Kodein
import org.kodein.di.bindings.Scope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MessagesObs = Flow<Message>
typealias WeatherComponent = ComponentV2<Message, Command, State>//(messages: MessagesObs) -> Flow<State>

fun weatherModule(scope: Scope<Fragment>, startLocation: Location): Kodein.Module {
    return Kodein.Module("weatherModule") {

        bind<Dependencies>() with singleton { Dependencies(instance(), instance(), instance()) }

        bind<WeatherComponent>() with scoped(scope).singleton {

            suspend fun resolver(command: Command) = instance<Dependencies>().resolveEffect(command)

            ComponentV2(State.Initial(startLocation), ::resolver, ::update)
        }
    }
}

@VisibleForTesting
fun update(message: Message, s: State): UpdateWith<State, Command> {
    return when (message) {
        is Message.LocationQueried -> State.Loading(s.location) command Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(s.location, message.weather).noCommand()
        is Message.OpFuckup -> State.LoadFailure(s.location, message.th).noCommand()
        Message.ViewAttached -> calculateInitialState(s)
        Message.SelectLocation -> s command Command.SelectLocation(s.location)
        Message.Retry -> calculateRetryAction(s)
    }
}

private data class Dependencies(
        val locationModel: LocationModel,
        val fragment: Fragment,
        val weatherProvider: WeatherProvider
)

private suspend fun Dependencies.resolveEffect(command: Command): Set<Message> {

    suspend fun resolve() = when (command) {
        is Command.LoadWeather -> effect { Message.WeatherLoaded(weatherProvider.fetchWeather(command.l)) }
        is Command.FeedLoaded -> effect { Message.WeatherLoaded(command.data) }
        is Command.SelectLocation -> sideEffect { fragment.navigateToMap(command.withSelectedLocation) }
    }

    return runCatching { resolve() }.getOrElse { th -> effect { Message.OpFuckup(th) } }
}

private fun calculateRetryAction(current: State): UpdateWith<State, Command> {
    return when (current) {
        is State.LoadFailure -> State.Loading(current.location) command Command.LoadWeather(current.location)
        is State.Loading, is State.Preview, is State.Initial -> throw IllegalStateException("Shouldn't get there, was $current")
    }
}

private fun calculateInitialState(current: State): UpdateWith<State, Command> {
    if (current is State.Initial) {
        return State.Loading(current.location) command Command.LoadWeather(current.location)
    }
    return current.noCommand()
}

private fun Fragment.navigateToMap(withStartLocation: Location?) {
    findNavController()
        .navigateDefaultAnimated(R.id.mapFragment,
                                 MapFragmentArgs(withStartLocation).toBundle())
}