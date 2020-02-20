package com.oliynick.max.elm.core.component

operator fun <S> Snapshot<*, S, *>.component1(): S = when(this) {
    is Initial -> currentState
    is Regular -> currentState
}

operator fun <C> Snapshot<*, *, C>.component2(): Set<C> = when(this) {
    is Initial -> commands
    is Regular -> commands
}