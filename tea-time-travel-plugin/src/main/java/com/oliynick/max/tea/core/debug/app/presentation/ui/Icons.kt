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

package com.oliynick.max.tea.core.debug.app.presentation.ui

/**
 * Plugin wide icon set
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ReflectionUtil
import org.jetbrains.skia.Image.Companion.makeFromEncoded
import javax.swing.Icon

object ValueIcon {
    val VariableIcon by lazy { getIcon("variable") }
    val ClassIcon by lazy { getIcon("class") }
    val PropertyIcon by lazy { getIcon("property") }
    val WatchIcon by lazy { getIcon("watch") }

    val VariableIconC @Composable get() = bitmap("variable")
    val ClassIconC @Composable get() = bitmap("class")
    val PropertyIconC @Composable get() = bitmap("property")
    val WatchIconC @Composable get() = bitmap("watch")
}

object ActionIcons {

    val UpdateRunningAppIcon by lazy { getIcon("updateRunningApplication") }
    val UpdateRunningAppIconC @Composable get() = bitmap("updateRunningApplication")
    val RemoveIcon by lazy { getIcon("remove") }
    val RemoveIconC @Composable get() = bitmap("remove")

    val RunDefaultIcon by lazy { getIcon("run") }
    val RunDefaultIconC @Composable get() = bitmap("run")
    val RunDisabledIcon by lazy { getIcon("run_disabled") }
    val RunDisabledIconC @Composable get() = bitmap("run_disabled")

    val ResumeIcon by lazy { getIcon("resume") }

    val CloseDefaultIcon by lazy { getIcon("close") }
    val CloseDefaultIconC @Composable get() = bitmap("close")
    val CloseDarkIcon by lazy { getIcon("close_dark") }

    val SuspendDefaultIconC @Composable get() = bitmap("suspend")
    val SuspendDisabledIcon by lazy { getIcon("suspend_disabled") }
    val SuspendDisabledIconC @Composable get() = bitmap("suspend_disabled")

    val StoppingIcon by lazy { getIcon("killProcess") }
    val StoppingIconC @Composable get() = bitmap("killProcess")
}

private fun resource(
    path: String,
) = (ReflectionUtil.getGrandCallerClass() ?: error("grand caller class == null")).getResource(path)
    ?: error("couldn't find resource for path $path")

@Composable
private fun bitmap(
    name: String
): ImageBitmap =
    if (PreviewMode.current) {
        ImageStub
    } else {
        remember(name) {
            makeFromEncoded(resource("/images/$name.png").readBytes()).asImageBitmap()
        }
    }

private fun getIcon(
    name: String,
): Icon = IconLoader.getIcon("/images/$name.png")

private const val StubImageSize = 80
private val ImageStub = ImageBitmap(StubImageSize, StubImageSize)