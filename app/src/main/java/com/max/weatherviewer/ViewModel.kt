package com.max.weatherviewer

import com.jakewharton.rxrelay2.BehaviorRelay
import com.max.weatherviewer.api.WeatherProvider
import com.max.weatherviewer.model.Location
import com.max.weatherviewer.model.Weather
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.ofType
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

//TODO add location fetching

typealias SearchFun = (Location) -> Single<Weather>
typealias ViewModel = (actions: Observable<out UserAction>) -> Observable<out State>

typealias UserActions = Observable<out UserAction>
typealias RefreshActions = Observable<UserAction.Refresh>
typealias ClickActions = Observable<UserAction.LoadButtonClicked>

//todo android Kodein
val viewModelModule = Kodein.Module("viewModel") {

    import(weatherPullerModule)

    bind<SearchFun>() with singleton {
        val searchFun: SearchFun = instance<WeatherProvider>()::fetchWeather

        searchFun
    }

    bind<ViewModel>() with singleton {
        val state = BehaviorRelay.createDefault<State>(State.Preview.empty())

        return@singleton { actions: Observable<out UserAction> ->
            bind(actions, state.value!!, instance()).doOnNext(state::accept)
        }
    }
}

inline fun bind(actions: UserActions, state: State, crossinline searchFun: SearchFun): Observable<State> {
    return actions.publish { shared ->
        Observable.merge(
            bind(shared.ofType<UserAction.LoadButtonClicked>(), searchFun),
            bind(shared.ofType<UserAction.Refresh>(), searchFun)
        )
    }
        .scan(state, BiFunction(::reduce))
}

fun reduce(@Suppress("UNUSED_PARAMETER") state: State, action: InternalAction): State {
    return when (action) {
        InternalAction.FeedLoading -> State.Loading
        is InternalAction.FeedLoaded -> State.Preview(action.data)
        is InternalAction.FeedLoadFailure -> State.Failure(action.th)
    }
}

@JvmName("bindForUserAction")
inline fun bind(actions: ClickActions, crossinline searchFun: SearchFun): Observable<out InternalAction> {
    return actions.flatMap<InternalAction> {
        //todo unhardcode
        searchFun(Location(30.0, 30.0)).flatMapObservable { Observable.just(InternalAction.FeedLoaded(it)) }
    }
        .onErrorReturn(InternalAction::FeedLoadFailure)
}

@JvmName("bindForRefresh")
inline fun bind(actions: RefreshActions, crossinline searchFun: SearchFun): Observable<out InternalAction> {
    return actions.flatMap<InternalAction> {
        //todo unhardcode
        searchFun(Location(30.0, 30.0)).flatMapObservable { Observable.just(InternalAction.FeedLoaded(it)) }
    }
        .onErrorReturn(InternalAction::FeedLoadFailure)
}