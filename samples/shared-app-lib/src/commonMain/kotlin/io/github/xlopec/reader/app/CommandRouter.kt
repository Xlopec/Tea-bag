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

package io.github.xlopec.reader.app

import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.command.ScreenCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Splits a shared command stream into one buffered mailbox per screen plus an app-wide mailbox.
 *
 * Replaces the previous design where each per-screen `TrackingEffect` filtered the same
 * `Flow<Command>`. With that design a screen's collector could miss commands emitted on the
 * snapshot that introduced it — the snapshots flow has replay-1 semantics, so a late
 * subscriber sees only the latest emission. Here, [route] is driven by a single collector
 * that runs continuously; each command lands in a buffered channel and is held until its
 * screen attaches.
 */
internal class CommandRouter {

    private val mutex = Mutex()
    private val mailboxes = mutableMapOf<ScreenId, Channel<Command>>()
    private val app = Channel<Command>(Channel.BUFFERED)

    suspend fun consumeScreen(id: ScreenId, block: suspend (Command) -> Unit) {
        for (command in mailboxFor(id)) block(command)
    }

    suspend fun consumeApp(block: suspend (Command) -> Unit) {
        for (command in app) block(command)
    }

    /**
     * Routes [command] to its mailbox. [liveIds] is the screen-id set of the snapshot the
     * command came from; screen-targeted commands for ids missing from that set are dropped,
     * which bounds leaks from mailboxes allocated for screens that never mount.
     */
    suspend fun route(command: Command, liveIds: Set<ScreenId>) {
        when {
            command is ScreenCommand && command.id != null -> {
                val id = command.id!!
                if (id !in liveIds) return
                mailboxFor(id).send(command)
            }
            else -> app.send(command)
        }
    }

    /**
     * Closes and removes any mailbox whose screen is no longer in [liveIds]. Call once per
     * snapshot, before [route]ing that snapshot's commands, so a departed screen's mailbox
     * is torn down deterministically.
     */
    suspend fun retainOnly(liveIds: Set<ScreenId>) = mutex.withLock {
        val iterator = mailboxes.entries.iterator()
        while (iterator.hasNext()) {
            val (id, channel) = iterator.next()
            if (id !in liveIds) {
                channel.close()
                iterator.remove()
            }
        }
    }

    private suspend fun mailboxFor(id: ScreenId): Channel<Command> = mutex.withLock {
        mailboxes.getOrPut(id) { Channel(Channel.BUFFERED) }
    }
}
