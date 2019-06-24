package com.max.weatherviewer.presentation.start

import com.google.android.gms.location.LocationRequest
import com.max.weatherviewer.api.location.LocationMessage
import com.max.weatherviewer.api.location.LocationModel
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather
import com.max.weatherviewer.api.weather.WeatherProvider
import com.max.weatherviewer.component.Component
import com.max.weatherviewer.just
import io.reactivex.Observable
import io.reactivex.Single
import org.kodein.di.Kodein
import org.kodein.di.android.ActivityRetainedScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

typealias MessagesObs = Observable<out Message>
typealias WeatherComponent = (messages: MessagesObs) -> Observable<out State>

fun weatherModule(startLocation: Location) = Kodein.Module("weatherModule") {

    bind<Resolver>() with singleton { ResolverImp(instance(), instance()) }

    bind<WeatherComponent>() with scoped(ActivityRetainedScope).singleton {

        fun resolver(command: Command): Single<Message> = instance<Resolver>().resolveEffect(command)

        Component(State.Preview(startLocation), ::resolver, ::update)
    }
}

interface Resolver {

    fun loadFeed(l: Location): Single<Weather>

    fun queryLocation(): Single<out LocationMessage>

}

fun update(message: Message, @Suppress("UNUSED_PARAMETER") s: State): Pair<State, Command> {
    return when (message) {
        is Message.LoadButtonClicked -> State.Loading(s.location) to Command.QueryLocation
        is Message.LocationQueried -> State.Loading(s.location) to Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(s.location, message.weather) to Command.None
        is Message.LoadFuckup -> State.Failure(s.location, message.th) to Command.None
        Message.PermissionFuckup -> State.PermissionRequestFuckup(s.location) to Command.None
        Message.RequestPermission -> State.RequestPermission(s.location) to Command.None
        Message.ShowPermissionRationale -> State.ShowPermissionRationale(s.location) to Command.None
    }
}

fun Resolver.resolveEffect(command: Command): Single<Message> {
    return when (command) {
        Command.None -> Single.never()
        is Command.LoadWeather -> loadFeed(command.l).map(Message::WeatherLoaded)
        is Command.FeedLoaded -> Message.WeatherLoaded(command.data).just()
        is Command.FeedLoadFailure -> Message.LoadFuckup(command.th).just()
        Command.PermissionRequestFuckup -> Message.PermissionFuckup.just()
        Command.ShowPermissionRationale -> Message.RequestPermission.just()
        Command.QueryLocation -> queryLocation().map(::toMessage)
    }
}

fun toMessage(locationMessage: LocationMessage): Message {
    return when (locationMessage) {
        LocationMessage.PermissionDenied -> Message.PermissionFuckup
        LocationMessage.ShowRationale -> Message.ShowPermissionRationale
        is LocationMessage.LocationResult -> Message.LocationQueried(locationMessage.l)
    }
}

private class ResolverImp(private val weatherProvider: WeatherProvider,
                          private val locationModel: LocationModel) : Resolver {

    private val locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setFastestInterval(1L)
        .setSmallestDisplacement(50f)

    override fun loadFeed(l: Location): Single<Weather> = weatherProvider.fetchWeather(l)

    override fun queryLocation(): Single<out LocationMessage> = locationModel(locationRequest)

}