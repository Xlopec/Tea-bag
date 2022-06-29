/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import io.github.xlopec.tea.time.travel.plugin.data.*
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
internal class UpdateForNotificationTest {

    @Test
    fun `test when message is NotifyStarted then plugin goes to a Started state`() {
        val (nextState, commands) = updateForNotification(
            NotifyStarted(StartedTestServerStub),
            State(ValidTestSettings)
        )

        assertEquals(
            State(
                ValidTestSettings,
                server = StartedTestServerStub,
            ), nextState
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = updateForNotification(
            NotifyStopped,
            State(
                ValidTestSettings,
                server = StartedTestServerStub,
            )
        )

        assertEquals(State(ValidTestSettings), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when append snapshot to non-existing component then it gets appended`() {
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val commandsWrapper = CollectionWrapper(listOf(message))
        val otherStates = ComponentDebugStates('b'..'z')
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)

        val (nextState, commands) = updateForNotification(
            AppendSnapshot(componentId, meta, message, oldState, newState, commandsWrapper),
            StartedFromPairs(otherStates)
        )

        val expectedDebugState = DebuggableComponent(
            componentId,
            newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState, commandsWrapper))
        )

        assertEquals(StartedFromPairs(otherStates + (componentId to expectedDebugState)), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when append snapshot to existing component then it gets appended`() {
        val otherStates = ComponentDebugStates('a'..'z') { strId ->

            val id = ComponentId(strId)

            if (id.value == "a") ComponentDebugState(id)
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp1))
        }

        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val commandsWrapper = CollectionWrapper(listOf())

        val (nextState, commands) = updateForNotification(
            AppendSnapshot(componentId, meta, message, oldState, newState, commandsWrapper),
            StartedFromPairs(otherStates)
        )

        val expectedDebugState = DebuggableComponent(
            componentId,
            newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState))
        )

        assertEquals(StartedFromPairs(otherStates + (componentId to expectedDebugState)), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when apply state then it gets applied`() {
        val otherStates = ComponentDebugStates('a'..'z') { strId ->
            val id = ComponentId(strId)

            if (id.value == "a") ComponentDebugState(id)
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp1))
        }

        val componentId = ComponentId("a")
        val newState = StringWrapper("d")

        val (nextState, commands) = updateForNotification(
            StateApplied(componentId, newState),
            StartedFromPairs(otherStates)
        )

        val expectedDebugState = DebuggableComponent(componentId, newState)

        assertEquals(
            StartedFromPairs(otherStates.takeLast(otherStates.size - 1) + (componentId to expectedDebugState)),
            nextState
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when apply state and component doesn't exist then it doesn't get applied`() {
        val state = StartedFromPairs(ComponentDebugStates())

        val (nextState, commands) = updateForNotification(StateApplied(ComponentId("a"), StringWrapper("d")), state)

        assertSame(state, nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `test when append new component then it gets appended`() {
        val otherStates = ComponentDebugStates { strId -> ComponentDebugState(ComponentId(strId)) }
        val componentId = ComponentId("a")
        val state = StringWrapper("d")
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val collectionWrapper = CollectionWrapper(listOf(StringWrapper("a")))

        val (nextState, commands) = updateForNotification(
            ComponentAttached(componentId, meta, state, collectionWrapper),
            StartedFromPairs(otherStates)
        )

        assertEquals(
            StartedFromPairs(
                otherStates +
                        ComponentDebugState(
                            componentId = componentId,
                            state = state,
                            snapshots = persistentListOf(
                                OriginalSnapshot(
                                    meta = meta,
                                    message = null,
                                    state = state,
                                    commands = collectionWrapper
                                )
                            ),
                            filteredSnapshots = persistentListOf(
                                FilteredSnapshot(
                                    meta = meta,
                                    message = null,
                                    state = state,
                                    commands = collectionWrapper
                                )
                            )
                        )
            ),
            nextState
        )

        assertEquals(setOf(DoNotifyComponentAttached(componentId)), commands)
    }

    @Test
    fun `test when illegal combination of message and state warning command is returned`() {
        val initialState = State(ValidTestSettings)
        val message = object : NotificationMessage {}
        val (state, commands) = updateForNotification(message, initialState)

        assertSame(initialState, state)
        assertEquals(setOf(DoWarnUnacceptableMessage(message, initialState)), commands)
    }
}
