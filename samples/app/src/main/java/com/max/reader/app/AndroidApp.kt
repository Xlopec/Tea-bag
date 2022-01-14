/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

// used by OS
@file:Suppress("unused", "FunctionName")

package com.max.reader.app

import android.app.Activity
import android.app.Application
import com.oliynick.max.reader.app.AppState
import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.message.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class AndroidApp : Application(), HasEnvironment {
    override val closeCommands = MutableSharedFlow<CloseApp>()
    override val component by lazy { AndroidAppComponent(this, AppComponentScope, closeCommands) }
}

object AppComponentScope : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Job() + Executors.newSingleThreadExecutor { r -> Thread(r, "App Scheduler") }
            .asCoroutineDispatcher()
}

inline val Activity.androidApp: HasEnvironment
    get() = application as HasEnvironment

inline val Activity.component: (Flow<Message>) -> Flow<AppState>
    get() = androidApp.component

inline val Activity.closeAppCommands: Flow<CloseApp>
    get() = androidApp.closeCommands
