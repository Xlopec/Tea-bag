// used by OS
@file:Suppress("unused")

package com.max.weatherviewer.app

import android.app.Activity
import android.app.Application
import com.max.weatherviewer.BuildConfig
import com.max.weatherviewer.app.env.Environment
import com.max.weatherviewer.misc.unsafeLazy
import com.oliynick.max.elm.core.component.Component
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class AndroidApp : Application() {

    val environment by unsafeLazy {
        Environment(this, BuildConfig.DEBUG)
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
