package com.oliynick.max.elm.core.component

import android.util.Log
import kotlinx.coroutines.flow.Flow

fun <M, S> androidLogger(component: (Flow<M>) -> Flow<S>,
                         tag: String = component::class.simpleName ?: component.toString()): (Flow<M>) -> Flow<S> {

    return LogComponent(
        { s -> Log.d(tag, "State: $s") },
        { m -> Log.d(tag, "Message: $m") },
        component
    )
}

//todo improve logger + add infix fun for that