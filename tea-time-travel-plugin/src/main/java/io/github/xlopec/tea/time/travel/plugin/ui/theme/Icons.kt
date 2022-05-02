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

package io.github.xlopec.tea.time.travel.plugin.ui.theme

/**
 * Plugin wide icon set
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ReflectionUtil
import io.github.xlopec.tea.time.travel.plugin.ui.PreviewMode
import javax.swing.Icon
import org.jetbrains.skia.Image.Companion.makeFromEncoded

object ValueIcon {
    val VariableIcon by lazy { getIcon("variable") }
    val ClassIcon by lazy { getIcon("class") }
    val PropertyIcon by lazy { getIcon("property") }
    val WatchIcon by lazy { getIcon("watch") }

    val Class @Composable get() = painterResource("/images/class.svg")
    val PropertyIconC @Composable get() = bitmap("property")
    val Property @Composable get() = painterResource("/images/property.svg")
    val Snapshot @Composable get() = painterResource("/images/snapshotGutter.svg")
}

object ActionIcons {

    @Deprecated("remove")
    val UpdateRunningAppIcon by lazy { getIcon("updateRunningApplication") }
    @Deprecated("remove")
    val UpdateRunningAppIconC @Composable get() = bitmap("updateRunningApplication")
    val UpdateRunningApplication @Composable get() = painterResource("/images/updateRunningApplication.svg")
    @Deprecated("remove")
    val RemoveIcon by lazy { getIcon("remove") }
    @Deprecated("remove")
    val RemoveIconC @Composable get() = bitmap("remove")
    val Remove @Composable get() = painterResource("/images/remove.svg")

    val CloseDefaultIconC @Composable get() = bitmap("close")
    val CloseDarkIcon by lazy { getIcon("close_dark") }

    val Import @Composable get() = painterResource("/images/import_dark.svg")
    val Export @Composable get() = painterResource("/images/export_dark.svg")
    val Execute @Composable get() = painterResource("/images/execute.svg")
    val Suspend @Composable get() = painterResource("/images/suspend.svg")
    val Close @Composable get() = painterResource("/images/close.svg")

    val Expand @Composable get() = painterResource("/images/expand.svg")
    val Collapse @Composable get() = painterResource("/images/collapse.svg")
    val Copy @Composable get() = painterResource("/images/inlineCopy.svg")
}

fun resource(
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
            makeFromEncoded(resource("/images/$name.png").readBytes()).toComposeImageBitmap()
        }
    }

private fun getIcon(
    name: String,
): Icon = IconLoader.getIcon("/images/$name.png")

private const val StubImageSize = 80

private val ImageStub = ImageBitmap(StubImageSize, StubImageSize)
