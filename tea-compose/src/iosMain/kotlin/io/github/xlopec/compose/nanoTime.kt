package io.github.xlopec.compose

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.posix.CLOCK_MONOTONIC_RAW
import platform.posix.clock_gettime_nsec_np

@OptIn(ExperimentalForeignApi::class)
internal actual fun nanoTime(): Long = clock_gettime_nsec_np(CLOCK_MONOTONIC_RAW.toUInt()).convert<Long>()
