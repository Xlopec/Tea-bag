/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// used by OS
@file:Suppress("unused")

package com.max.reader.app

import android.app.Activity
import android.app.Application
import com.max.reader.app.env.Environment
import com.max.reader.misc.unsafeLazy
import com.max.reader.ui.isDarkModeEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AndroidApp : Application(), CoroutineScope by AppComponentScope {

    val environment by unsafeLazy {
        Environment(this, this)
    }

    val component by unsafeLazy { environment.AppComponent(AppInitializer(isDarkModeEnabled)) }

    val messages = BroadcastChannel<Message>(1)

}

private object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Job() + Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}

inline val Activity.androidApp: AndroidApp
    get() = application as AndroidApp

inline val Activity.appComponent: (Flow<Message>) -> Flow<AppState>
    get() = androidApp.component

inline val Activity.appMessages: BroadcastChannel<Message>
    get() = androidApp.messages

inline val Activity.environment: Environment
    get() = androidApp.environment

inline val Activity.closeAppCommands: Flow<CloseApp>
    get() = androidApp.environment.closeCommands.asFlow()
