package io.github.xlopec.tea.core.misc

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.linux.PR_GET_NAME
import platform.linux.prctl

// Linux thread names are max 16 chars including null terminator
private const val NameBufferSize = 16

@OptIn(ExperimentalForeignApi::class)
actual fun currentThreadName(): String {
    memScoped {
        val nameBuffer = allocArray<ByteVar>(NameBufferSize)
        val ret = prctl(PR_GET_NAME, nameBuffer)
        if (ret == 0) {
            return nameBuffer.toKString()
        }
        // Handle error, e.g., ERANGE if the buffer is too small
        error("Error getting thread name: $ret")
    }
}