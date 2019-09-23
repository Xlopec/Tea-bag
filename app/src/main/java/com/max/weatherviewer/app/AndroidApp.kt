@file:Suppress("unused") // used by OS

package com.max.weatherviewer.app

import android.app.Application
import com.max.weatherviewer.api.weather.weatherModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule

class AndroidApp : Application(), KodeinAware {
    override val kodein: Kodein = Kodein.lazy {
        importOnce(androidXModule(this@AndroidApp))
        importOnce(weatherModule)
    }
}