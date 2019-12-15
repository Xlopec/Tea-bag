// used by OS
@file:Suppress("unused")

package com.max.weatherviewer.app

import android.app.Activity
import android.app.Application
import com.max.weatherviewer.BuildConfig
import com.max.weatherviewer.app.env.Environment
import com.max.weatherviewer.app.env.gson
import com.max.weatherviewer.app.env.network.adapters
import com.max.weatherviewer.app.env.retrofit
import com.oliynick.max.elm.core.component.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AndroidApp : Application() {

    val environment by unsafeLazy {
        Environment(
            retrofit,
            AppComponentScope,
            this,
            gson,
            BuildConfig.DEBUG
        )
    }

    val component by unsafeLazy { environment.appComponent() }

    val messages = BroadcastChannel<Message>(1)
}

inline val Activity.androidApp: AndroidApp
    get() = application as AndroidApp

inline val Activity.appComponent: Component<Message, State>
    get() = androidApp.component

inline val Activity.appMessages: BroadcastChannel<Message>
    get() = androidApp.messages

inline val Activity.environment: Environment
    get() = androidApp.environment

inline val Activity.closeAppCommands: Flow<CloseApp>
    get() = androidApp.environment.closeCommands.asFlow()

private val gson by unsafeLazy {
    gson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        adapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }
}

private val retrofit by unsafeLazy {
    retrofit(gson)
}

private fun <T> unsafeLazy(block: () -> T) = lazy(LazyThreadSafetyMode.NONE, block)

private object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}