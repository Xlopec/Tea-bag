package com.oliynick.max.tea.core

operator fun <S> Snapshot<*, S, *>.component1(): S = when (this) {
    is Initial -> currentState
    is Regular -> currentState
}

operator fun <C> Snapshot<*, *, C>.component2(): Set<C> = when (this) {
    is Initial -> commands
    is Regular -> commands
}

operator fun <S> Snapshot<*, S, *>.component3(): S? = when (this) {
    is Initial -> null
    is Regular -> previousState
}

operator fun <M> Snapshot<M, *, *>.component4(): M? = when (this) {
    is Initial -> null
    is Regular -> message
}