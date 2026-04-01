package io.github.xlopec.tea.core.misc

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.pthread_self

@OptIn(ExperimentalForeignApi::class)
actual fun currentThreadName(): String {
    return "Thread ${pthread_self()}"
}