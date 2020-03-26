package com.oliynick.max.tea.core.debug.app.presentation.misc

/**
 * Plugin wide icon set
 */

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object ValueIcon {
    val VARIABLE_ICON by unsafeLazy { getIcon("variable") }
    val CLASS_ICON by unsafeLazy { getIcon("class") }
    val PROPERTY_ICON by unsafeLazy { getIcon("property") }
    val WATCH_ICON by unsafeLazy { getIcon("watch") }
}

object ActionIcons {

    val UPDATE_RUNNING_APP_ICON by unsafeLazy { getIcon("updateRunningApplication") }
    val REMOVE_ICON by unsafeLazy { getIcon("remove") }

    val RUN_DEFAULT_ICON by unsafeLazy { getIcon("run") }
    val RUN_DISABLED_ICON by unsafeLazy { getIcon("run_disabled") }

    val RESUME_ICON by unsafeLazy { getIcon("resume") }

    val CLOSE_DEFAULT_ICON by unsafeLazy { getIcon("close") }
    val CLOSE_DARK_ICON by unsafeLazy { getIcon("close_dark") }

    val SUSPEND_DEFAULT_ICON by unsafeLazy { getIcon("suspend") }
    val SUSPEND_DISABLED_ICON by unsafeLazy { getIcon("suspend_disabled") }

    val STOPPING_ICON by unsafeLazy { getIcon("killProcess") }
}

private fun getIcon(
    name: String
): Icon = IconLoader.getIcon("/icons/$name.png")

private fun <T> unsafeLazy(
    provider: () -> T
) = lazy(LazyThreadSafetyMode.NONE, provider)
