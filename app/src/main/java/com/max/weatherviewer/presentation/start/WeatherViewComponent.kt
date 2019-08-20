package com.max.weatherviewer.presentation.start

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
import com.max.weatherviewer.just
import com.max.weatherviewer.navigateDefaultAnimated
import com.max.weatherviewer.presentation.map.MapFragmentArgs
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.kodein.di.Kodein
import org.kodein.di.bindings.Scope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MessagesObs = Observable<out Message>
typealias WeatherComponent = (messages: MessagesObs) -> Observable<out State>

fun weatherModule(scope: Scope<Fragment>, startLocation: Location) = Kodein.Module("weatherModule") {

    bind<Navigator>() with scoped(scope).singleton { Navigator(instance()) }

    bind<Resolver>() with singleton { ResolverImp(instance(), instance(), instance()) }

    bind<WeatherComponent>() with scoped(scope).singleton {

        fun resolver(command: Command): Single<Message> = instance<Resolver>().resolveEffect(command)

        Component(State.Initial(startLocation), ::resolver, ::update)
    }
}

@Deprecated("todo: remove, will be replaced with a curried function")
interface Resolver {

    fun loadFeed(l: Location): Single<Weather>

    fun queryLocation(): Single<out LocationMessage>

    fun toLocationSelection(withStartLocation: Location?)
}

fun update(message: Message, s: State): Pair<State, Command> {
    return when (message) {
        is Message.LoadButtonClicked -> State.Loading(s.location) to Command.QueryLocation
        is Message.LocationQueried -> State.Loading(s.location) to Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(s.location, message.weather) to Command.None
        is Message.OpFuckup -> State.LoadFailure(s.location, message.th) to Command.None
        Message.PermissionFuckup -> State.PermissionRequestFuckup(s.location) to Command.None
        Message.RequestPermission -> State.RequestPermission(s.location) to Command.None
        Message.ShowPermissionRationale -> State.ShowPermissionRationale(s.location) to Command.None
        Message.ViewAttached -> calculateInitialState(s)
        Message.SelectLocation -> s to Command.SelectLocation(s.location)
        Message.Retry -> calculateRetryAction(s)
    }
}

fun Resolver.resolveEffect(command: Command): Single<Message> {
    val source: Single<Message> = when (command) {
        Command.None -> Single.never()
        is Command.LoadWeather -> loadFeed(command.l).map(Message::WeatherLoaded)
        is Command.FeedLoaded -> Message.WeatherLoaded(command.data).just()
        Command.PermissionRequestFuckup -> Message.PermissionFuckup.just()
        Command.ShowPermissionRationale -> Message.RequestPermission.just()
        Command.QueryLocation -> queryLocation().map(::toMessage)
        is Command.SelectLocation -> Completable.fromAction { toLocationSelection(command.withSelectedLocation) }.andThen(Single.never())
    }

    return source.onErrorReturn(Message::OpFuckup)
}

fun toMessage(locationMessage: LocationMessage): Message {
    return when (locationMessage) {
        LocationMessage.PermissionDenied -> Message.PermissionFuckup
        LocationMessage.ShowRationale -> Message.ShowPermissionRationale
        is LocationMessage.LocationResult -> Message.LocationQueried(locationMessage.l)
    }
}

private class ResolverImp(private val weatherProvider: WeatherProvider,
                          private val locationModel: LocationModel,
                          private val navigator: Navigator) : Resolver {

    private val locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setFastestInterval(1L)
        .setSmallestDisplacement(50f)

    override fun toLocationSelection(withStartLocation: Location?) = navigator.navigateToMap(withStartLocation)

    override fun loadFeed(l: Location): Single<Weather> = weatherProvider.fetchWeather(l)

    override fun queryLocation(): Single<out LocationMessage> = locationModel(locationRequest)

}

private fun calculateRetryAction(current: State): Pair<State, Command> {
    return when (current) {
        is State.LoadFailure -> State.Loading(current.location) to Command.LoadWeather(current.location)
        is State.PermissionRequestFuckup -> TODO()
        is State.ShowPermissionRationale -> TODO()
        is State.RequestPermission -> TODO()
        is State.Loading, is State.Preview, is State.Initial -> throw IllegalStateException("Shouldn't get there, was $current")
    }
}

private fun calculateInitialState(current: State): Pair<State, Command> {
    if (current is State.Initial) {
        return State.Loading(current.location) to Command.LoadWeather(current.location)
    }
    return current to Command.None
}

private class Navigator(private val fragment: Fragment) {

    fun navigateToMap(withStartLocation: Location?) {
        fragment.findNavController()
            .navigateDefaultAnimated(R.id.mapFragment, MapFragmentArgs.Builder(withStartLocation).build().toBundle())
    }

}