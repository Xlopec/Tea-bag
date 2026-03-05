package io.github.xlopec.tea.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Tracking effect that runs a [block] in a [TrackingScope] when the [key] changes.
 */
@Composable
public fun TrackingEffect(key: Any?, block: suspend TrackingScope.() -> Unit) {
    LaunchedEffect(key) {
        block(TrackingScope(coroutineContext))
    }
}

/**
 * Tracking effect that runs a [block] in a [TrackingScope] when [key1] or [key2] changes.
 */
@Composable
public fun TrackingEffect(key1: Any?, key2: Any?, block: suspend TrackingScope.() -> Unit) {
    LaunchedEffect(key1, key2) {
        block(TrackingScope(coroutineContext))
    }
}

/**
 * Tracking effect that runs a [block] in a [TrackingScope] when [key1], [key2], or [key3] changes.
 */
@Composable
public fun TrackingEffect(key1: Any?, key2: Any?, key3: Any?, block: suspend TrackingScope.() -> Unit) {
    LaunchedEffect(key1, key2, key3) {
        block(TrackingScope(coroutineContext))
    }
}

/**
 * Tracking effect that runs a [block] in a [TrackingScope] when any of the [keys] change.
 */
@Composable
public fun TrackingEffect(vararg keys: Any?, block: suspend TrackingScope.() -> Unit) {
    LaunchedEffect(keys = keys) {
        block(TrackingScope(coroutineContext))
    }
}
