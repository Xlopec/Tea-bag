package com.max.weatherviewer

import com.max.weatherviewer.api.WeatherProvider
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val weatherPullerModule = Kodein.Module("weatherPuller") {
    bind<WeatherProvider>() with singleton {
        WeatherProvider("b40db1b95c75e4668ab28ed46a6c6c45", 60)
    }

    bind<Observable<UserAction>>() with singleton { PublishRelay.create<UserAction>() }
}