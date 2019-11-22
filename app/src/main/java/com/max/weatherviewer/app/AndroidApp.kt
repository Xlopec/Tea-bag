@file:Suppress("unused") // used by OS

package com.max.weatherviewer.app

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

object ComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext = Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }.asCoroutineDispatcher()
}

class AndroidApp : Application() {


}
