package io.github.xlopec.tea.compose

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_MONOTONIC
import platform.posix.clock_gettime
import platform.posix.timespec

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalForeignApi::class)
internal actual inline fun nanoTime(): Long = memScoped {
    val timespec = alloc<timespec>()
    clock_gettime(CLOCK_MONOTONIC, timespec.ptr)
    timespec.tv_sec * 1_000_000L + timespec.tv_nsec
}