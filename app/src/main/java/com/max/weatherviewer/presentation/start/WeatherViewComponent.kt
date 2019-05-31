package com.max.weatherviewer.presentation.start

import com.google.android.gms.location.LocationRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import com.max.weatherviewer.api.location.LocationMessage
import com.max.weatherviewer.api.location.LocationModel
import com.max.weatherviewer.api.location.locationModule
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather
import com.max.weatherviewer.api.weather.WeatherProvider
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
typealias StateHolder = BehaviorRelay<State>
typealias WeatherModel = (messages: MessagesObs) -> Observable<out State>

val weatherModule = Kodein.Module("weatherModule") {
    val state = BehaviorRelay.createDefault<State>(State.Preview.empty())

    import(locationModule)

    bind<EffectResolver>() with singleton { EffectsResolverImp(instance(), instance()) }

    bind<WeatherModel>() with scoped(ActivityRetainedScope).singleton { WeatherComponent(state, instance()) }
}

interface EffectResolver {

    fun loadFeed(l: Location): Single<Weather>

    fun queryLocation(): Single<out LocationMessage>

}

class WeatherComponent(private val state: StateHolder,
                       private val effectResolver: EffectResolver) : WeatherModel {

    override fun invoke(messages: MessagesObs): Observable<out State> {
        val initial = state.value!!

        return messages.map { message -> update(message, initial) }
            .flatMap { (nextState, effect) ->
                effectResolver.resolveEffect(effect)
                    .flatMapObservable { msg -> invoke(Observable.just(msg)) }
                    .startWith(nextState)
            }
            .doOnNext(state::accept)
            .startWith(initial)
            .distinctUntilChanged()
    }

}

fun update(message: Message, @Suppress("UNUSED_PARAMETER") state: State): Pair<State, Command> {
    return when (message) {
        is Message.LoadButtonClicked -> State.Loading to Command.QueryLocation
        is Message.LocationQueried -> State.Loading to Command.LoadWeather(message.l)
        is Message.WeatherLoaded -> State.Preview(message.weather) to Command.None
        is Message.LoadFuckup -> State.Failure(message.th) to Command.None
        Message.PermissionFuckup -> State.PermissionRequestFuckup to Command.None
        Message.RequestPermission -> State.RequestPermission to Command.None
        Message.ShowPermissionRationale -> State.ShowPermissionRationale to Command.None
    }
}

fun EffectResolver.resolveEffect(command: Command): Single<Message> {
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

private class EffectsResolverImp(private val weatherProvider: WeatherProvider,
                                 private val locationModel: LocationModel) : EffectResolver {

    private val locationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setFastestInterval(1L)
        .setSmallestDisplacement(50f)

    override fun loadFeed(l: Location): Single<Weather> = weatherProvider.fetchWeather(l)

    override fun queryLocation(): Single<out LocationMessage> = locationModel(locationRequest)

}