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

package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

/**
 * Plugin wide icon set
 */

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object ValueIcon {
    val VariableIcon by unsafeLazy { getIcon("variable") }
    val ClassIcon by unsafeLazy { getIcon("class") }
    val PropertyIcon by unsafeLazy { getIcon("property") }
    val WatchIcon by unsafeLazy { getIcon("watch") }
}

object ActionIcons {

    val UpdateRunningAppIcon by unsafeLazy { getIcon("updateRunningApplication") }
    val RemoveIcon by unsafeLazy { getIcon("remove") }

    val RunDefaultIcon by unsafeLazy { getIcon("run") }
    val RunDisabledIcon by unsafeLazy { getIcon("run_disabled") }

    val ResumeIcon by unsafeLazy { getIcon("resume") }

    val CloseDefaultIcon by unsafeLazy { getIcon("close") }
    val CloseDarkIcon by unsafeLazy { getIcon("close_dark") }

    val SuspendDefaultIcon by unsafeLazy { getIcon("suspend") }
    val SuspendDisabledIcon by unsafeLazy { getIcon("suspend_disabled") }

    val StoppingIcon by unsafeLazy { getIcon("killProcess") }
}

private fun getIcon(
    name: String
): Icon = IconLoader.getIcon("/icons/$name.png")

private fun <T> unsafeLazy(
    provider: () -> T
) = lazy(LazyThreadSafetyMode.NONE, provider)
