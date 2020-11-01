// used by OS
@file:Suppress("unused")

package com.max.reader.app

import android.app.Activity
import android.app.Application
import com.max.reader.BuildConfig
import com.max.reader.app.env.Environment
import com.max.reader.misc.unsafeLazy
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class AndroidApp : Application() {

    val environment by unsafeLazy {
        Environment(this, BuildConfig.DEBUG)
    }

    val component by unsafeLazy { environment.appComponent(AppInitializer()) }

    val messages = BroadcastChannel<Message>(1)
}

inline val Activity.androidApp: AndroidApp
    get() = application as AndroidApp

inline val Activity.appComponent: (Flow<Message>) -> Flow<State>
    get() = androidApp.component

inline val Activity.appMessages: BroadcastChannel<Message>
    get() = androidApp.messages

inline val Activity.environment: Environment
    get() = androidApp.environment

inline val Activity.closeAppCommands: Flow<CloseApp>
    get() = androidApp.environment.closeCommands.asFlow()
