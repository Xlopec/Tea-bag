package com.max.weatherviewer.api.location

import android.app.Activity
import com.jakewharton.rxrelay2.PublishRelay
import com.max.weatherviewer.api.weather.Location
import org.kodein.di.Kodein
import org.kodein.di.bindings.WeakContextScope
import org.kodein.di.generic.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

val locationModule = Kodein.Module("location") {

    bind<LocationPublisher>() with singleton { PublishRelay.create<Location>() }

    bind<LocationObserver>() with provider { instance<LocationPublisher>() }

    bind<PermissionPublisher>() with singleton { PublishRelay.create<PermissionResult>() }

    bind<PermissionObserver>() with provider { instance<PermissionPublisher>() }

    bind<LibLocationProvider>() with singleton { ReactiveLocationProvider(instance()) }

    bind<LocationModel>() with scoped(WeakContextScope.of<Activity>()).singleton {
        LocationComponent(instance(), instance(), instance(tag = Activity::class))
    }

}