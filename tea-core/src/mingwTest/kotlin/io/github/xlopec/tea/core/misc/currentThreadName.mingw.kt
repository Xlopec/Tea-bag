package io.github.xlopec.tea.core.misc

import platform.windows.GetCurrentThreadId

actual fun currentThreadName(): String {
    return "Thread-${GetCurrentThreadId()}"
}
