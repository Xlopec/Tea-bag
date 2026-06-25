/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.tea.compose

import io.github.xlopec.tea.core.ExperimentalTeaApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Splits a shared command stream into one buffered mailbox per [K] key plus a default mailbox
 * keyed by `null`.
 *
 * The router bridges a single hot snapshot stream to a dynamically changing set of per-key
 * consumers — for example, one consumer per screen on a Compose navigation stack. Each
 * mailbox is a buffered [Channel], so commands routed before a consumer attaches are held
 * until consumption; this avoids the late-subscriber miss that a shared `Flow` would have.
 *
 * Typical wiring:
 * ```
 * val router = remember {
 *     CommandRouter<ScreenId, Command> { (it as? ScreenCommand)?.id }
 * }
 * TrackingEffect(router) {
 *     snapshots.collect { s ->
 *         val live = s.currentState.screens.mapTo(HashSet()) { it.id }
 *         router.dispatch(live, s.commands)
 *     }
 * }
 * screens.fastForEach { screen ->
 *     key(screen.id) {
 *         TrackingEffect(screen.id) {
 *             router.consume(screen.id) { resolve(it) }
 *         }
 *     }
 * }
 * TrackingEffect(router) {
 *     router.consume(key = null) { resolve(it) } // default mailbox
 * }
 * ```
 *
 * @param K type of the routing key (e.g. a screen id)
 * @param C type of the command
 * @param keyOf extracts the destination key from a command; return `null` to route to the
 *              default mailbox (useful for commands that aren't tied to a specific consumer)
 */
@ExperimentalTeaApi
public class CommandRouter<K, C>(
    private val perChannelCapacity: Int = Channel.BUFFERED,
    private val keyOf: (C) -> K?,
) {

    private val mutex = Mutex()
    private val mailboxes = mutableMapOf<K?, Channel<C>>()

    /**
     * Consumes the mailbox for [key], or the default mailbox if [key] is `null`. Suspends
     * until the surrounding coroutine is cancelled or [dispatch] closes the mailbox.
     */
    public suspend fun consume(key: K?, block: suspend (C) -> Unit) {
        for (command in mailboxFor(key)) block(command)
    }

    /**
     * Routes [commands] to their mailboxes and closes mailboxes whose key is no longer in
     * [liveKeys]. Commands whose key (extracted via the constructor's `keyOf`) is non-null
     * but missing from [liveKeys] are dropped — this bounds leaks from mailboxes that would
     * otherwise be allocated for keys no consumer attaches to. The default mailbox
     * (key `null`) is never closed.
     */
    public suspend fun dispatch(liveKeys: Set<K>, commands: Iterable<C>) {
        retainOnly(liveKeys)
        for (command in commands) route(command, liveKeys)
    }

    private suspend fun route(command: C, liveKeys: Set<K>) {
        val key = keyOf(command)
        if (key != null && key !in liveKeys) return
        mailboxFor(key).send(command)
    }

    private suspend fun retainOnly(liveKeys: Set<K>) = mutex.withLock {
        val iterator = mailboxes.entries.iterator()
        while (iterator.hasNext()) {
            val (key, channel) = iterator.next()
            if (key != null && key !in liveKeys) {
                channel.close()
                iterator.remove()
            }
        }
    }

    private suspend fun mailboxFor(key: K?): Channel<C> = mutex.withLock {
        mailboxes.getOrPut(key) { Channel(perChannelCapacity) }
    }
}
