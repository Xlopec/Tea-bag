/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import android.Manifest.permission.ACCESS_LOCAL_NETWORK
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.CINNAMON_BUN
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import io.github.xlopec.reader.BuildConfig
import kotlinx.coroutines.CoroutineScope

fun AppComponent(
    application: Application,
    scope: CoroutineScope,
) = Environment(BuildConfig.DEBUG, application, scope)
    .let { env -> DebuggableAppComponent(env, AppInitializer(application.systemDarkModeEnabled, env)) }

fun ComponentActivity.afterRequiredPermissions(block: () -> Unit) {
    if (SDK_INT < CINNAMON_BUN || checkSelfPermission(ACCESS_LOCAL_NETWORK) == PERMISSION_GRANTED) {
        block()
    } else {
        registerForActivityResult(RequestPermission()) { block() }.launch(ACCESS_LOCAL_NETWORK)
    }
}
