package com.max.weatherviewer.api.location

import android.app.Activity
import com.jakewharton.rxrelay2.PublishRelay
import com.max.weatherviewer.api.weather.Location
import org.kodein.di.Kodein
import org.kodein.di.bindings.WeakContextScope
import org.kodein.di.generic.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

val locationModule = Kodein.Module("location") {

    bind<LocationPublisher>("location") with singleton { PublishRelay.create<Location>() }

    bind<LocationObserver>("location") with provider { instance<LocationPublisher>() }

    bind<PermissionPublisher>("permission") with singleton { PublishRelay.create<PermissionResult>() }

    bind<PermissionObserver>("permission") with provider { instance<PermissionPublisher>() }

    bind<LibLocationProvider>() with singleton { ReactiveLocationProvider(instance()) }

    bind<LocationModel>() with scoped(WeakContextScope.of<Activity>()).singleton {
        LocationComponent(instance(), instance(), instance(tag = Activity::class))
    }

}