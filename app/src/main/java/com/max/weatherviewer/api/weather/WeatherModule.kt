package com.max.weatherviewer.api.weather

import com.jakewharton.rxrelay2.PublishRelay
import com.max.weatherviewer.presentation.start.Message
import io.reactivex.Observable
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.generic.with

val weatherModule = Kodein.Module("weatherPuller") {

    constant("apiKey") with "b40db1b95c75e4668ab28ed46a6c6c45"

    bind<WeatherProvider>() with singleton { WeatherProvider(instance("apiKey"), 60) }

    bind<Observable<Message>>() with singleton { PublishRelay.create<Message>() }
}