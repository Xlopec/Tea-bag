package com.max.weatherviewer.presentation.viewer

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationRequest
import com.max.weatherviewer.R
import com.max.weatherviewer.api.location.LocationMessage
import com.max.weatherviewer.api.location.LocationModel
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather
import com.max.weatherviewer.api.weather.WeatherProvider
import com.max.weatherviewer.component.Component
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
typealias WeatherComponent = (messages: MessagesObs) -> Flow<State>

fun weatherModule(scope: Scope<Fragment>, startLocation: Location): Kodein.Module {
    return Kodein.Module("weatherModule") {

        bind<Navigator>() with scoped(scope).singleton { Navigator(instance()) }

        bind<Resolver>() with singleton { ResolverImp(instance(), instance(), instance()) }

        bind<WeatherComponent>() with scoped(scope).singleton {

            suspend fun resolver(command: Command) = instance<Resolver>().resolveEffect(command)

            Component(State.Initial(startLocation), ::resolver, ::update)
        }
    }
}

@Deprecated("todo: remove, will be replaced with a curried function")
interface Resolver {

    suspend fun loadFeed(l: Location): Weather

    suspend fun queryLocation(): LocationMessage

    suspend fun toLocationSelection(withStartLocation: Location?)
}

fun update(message: Message, s: State): Pair<State, Command?> {
    return when (message) {
        is Message.LocationQueried -> State.Loading(s.location) to Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(s.location, message.weather) to null
        is Message.OpFuckup -> State.LoadFailure(s.location, message.th) to null
        Message.ViewAttached -> calculateInitialState(s)
        Message.SelectLocation -> s to Command.SelectLocation(s.location)
        Message.Retry -> calculateRetryAction(s)
    }
}

suspend fun Resolver.resolveEffect(command: Command): Message? {

    suspend fun resolve() = when (command) {
        is Command.LoadWeather -> Message.WeatherLoaded(loadFeed(command.l))
        is Command.FeedLoaded -> Message.WeatherLoaded(command.data)
        is Command.SelectLocation -> { toLocationSelection(command.withSelectedLocation); null }
    }

    return runCatching { resolve() }.getOrElse(Message::OpFuckup)
}

private class ResolverImp(private val weatherProvider: WeatherProvider,
                          private val locationModel: LocationModel,
                          private val navigator: Navigator) :
    Resolver {

    private val locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setFastestInterval(1L)
        .setSmallestDisplacement(50f)

    override suspend fun toLocationSelection(withStartLocation: Location?) =
        navigator.navigateToMap(withStartLocation)

    override suspend fun loadFeed(l: Location) = weatherProvider.fetchWeather(l)

    override suspend fun queryLocation() = locationModel(locationRequest)

}

private fun calculateRetryAction(current: State): Pair<State, Command> {
    return when (current) {
        is State.LoadFailure -> State.Loading(
            current.location) to Command.LoadWeather(
            current.location)
        is State.Loading, is State.Preview, is State.Initial -> throw IllegalStateException("Shouldn't get there, was $current")
    }
}

private fun calculateInitialState(current: State): Pair<State, Command?> {
    if (current is State.Initial) {
        return State.Loading(current.location) to Command.LoadWeather(
            current.location)
    }
    return current to null
}

private class Navigator(private val fragment: Fragment) {

    fun navigateToMap(withStartLocation: Location?) {
        fragment.findNavController()
            .navigateDefaultAnimated(R.id.mapFragment,
                                     MapFragmentArgs(withStartLocation).toBundle())
    }

}