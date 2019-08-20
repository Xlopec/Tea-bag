package com.max.weatherviewer.component

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.Single

typealias Update<M, S, C> = (message: M, state: S) -> Pair<S, C>
typealias Resolver<C, M> = (command: C) -> Single<out M>

class Component<M, C, S>(initialState: S,
                         val resolver: Resolver<C, M>,
                         val update: Update<M, S, C>) : (Observable<out M>) -> Observable<out S> {

    private val state: BehaviorRelay<S> = BehaviorRelay.createDefault(initialState)

    override fun invoke(messages: Observable<out M>): Observable<out S> {
        return messages.map { message -> update(message, state.value!!) }
            .flatMap { (nextState, effect) ->
                resolver(effect)
                    .flatMapObservable { msg -> invoke(Observable.just(msg)) }
                    .startWith(nextState)
            }
            .startWith(state.value!!)
            .doOnNext(state::accept)
            .distinctUntilChanged()
            .doOnNext { Log.d(this@Component.javaClass.simpleName,"State $it") }
    }

}