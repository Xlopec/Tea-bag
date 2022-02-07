package com.max.reader.app.ui.misc

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import java.util.concurrent.atomic.AtomicLong

val LocalLogCompositions = compositionLocalOf { false }

@Composable
inline fun LogCompositions(
    tag: String
) {
    if (LocalLogCompositions.current) {
        val ref = remember { AtomicLong(0) }
        SideEffect { ref.incrementAndGet() }
        Log.i(tag, "compositions: $ref")
    }
}