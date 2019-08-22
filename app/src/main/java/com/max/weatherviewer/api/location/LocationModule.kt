package com.max.weatherviewer.api.location

import android.app.Activity
import com.max.weatherviewer.api.weather.Location
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

val locationModule = Kodein.Module("location") {

    bind<LocationPublisher>("location") with singleton { BroadcastChannel<Location>(1) }

    bind<LocationObserver>("location") with provider { instance<LocationPublisher>().asFlow() }

    bind<PermissionPublisher>() with singleton { BroadcastChannel<PermissionResult>(1) }

    bind<PermissionObserver>() with provider { instance<PermissionPublisher>().asFlow() }

    bind<LibLocationProvider>() with singleton { ReactiveLocationProvider(instance()) }

    bind<LocationModel>() with singleton {
        LocationComponent(instance(), instance(), instance(tag = Activity::class))
    }

}