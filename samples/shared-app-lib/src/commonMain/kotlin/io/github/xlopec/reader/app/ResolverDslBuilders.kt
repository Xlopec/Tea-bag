package io.github.xlopec.reader.app

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Wrapper to perform side effect computations and possibly return a new message to be consumed by [Updater]
 *
 * @receiver command to be used to execute effect
 * @param C command
 * @param M message
 * @param action action to perform that might produce message to be consumed by a component
 * @return set of messages to be consumed a component
 */
suspend inline infix fun <C, M> C.effect(
    crossinline action: suspend C.() -> M?,
): Set<M> {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return setOfNotNull(action(this))
}

suspend inline infix fun <C> C.sideEffect(
    crossinline action: suspend C.() -> Unit,
): Set<Nothing> {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    action(this)
    return setOf()
}
