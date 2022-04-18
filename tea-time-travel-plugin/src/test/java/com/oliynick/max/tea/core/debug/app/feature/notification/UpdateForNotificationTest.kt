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

package com.oliynick.max.tea.core.debug.app.feature.notification

import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.app.state.Stopped
import io.github.xlopec.tea.core.debug.protocol.ComponentId
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class UpdateForNotificationTest {

    @Test
    fun `test when message is NotifyStarted then plugin goes to a Started state`() {

        val (nextState, commands) = updateForNotification(NotifyStarted(StartedTestServerStub), Stopped(TestSettings))

        nextState shouldBe com.oliynick.max.tea.core.debug.app.state.Started(
            TestSettings,
            DebugState(),
            StartedTestServerStub
        )
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = updateForNotification(
            NotifyStopped,
            com.oliynick.max.tea.core.debug.app.state.Started(
                TestSettings,
                DebugState(),
                StartedTestServerStub
            )
        )

        nextState shouldBe Stopped(TestSettings)
        commands.shouldBeEmpty()
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
            Started(otherStates)
        )

        val expectedDebugState = ComponentDebugState(
            componentId,
            newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState, commandsWrapper))
        )

        nextState shouldBe Started(otherStates + (componentId to expectedDebugState))
        commands.shouldBeEmpty()
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
            Started(otherStates)
        )

        val expectedDebugState = ComponentDebugState(
            componentId,
            newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState))
        )

        nextState shouldBe Started(otherStates + (componentId to expectedDebugState))
        commands.shouldBeEmpty()
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
            Started(otherStates)
        )

        val expectedDebugState = ComponentDebugState(componentId, newState)

        nextState shouldBe Started(otherStates.takeLast(otherStates.size - 1) + (componentId to expectedDebugState))
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when apply state and component doesn't exist then it doesn't get applied`() {

        val state = Started(ComponentDebugStates())

        val (nextState, commands) = updateForNotification(StateApplied(ComponentId("a"), StringWrapper("d")), state)

        nextState shouldBeSameInstanceAs state
        commands.shouldBeEmpty()
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
            Started(otherStates)
        )

        nextState shouldBe Started(
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
        )

        commands.shouldContainExactly(DoNotifyComponentAttached(componentId))
    }

    @Test
    fun `test when illegal combination of message and state warning command is returned`() {

        val initialState = Stopped(TestSettings)
        val message = StateApplied(ComponentId("a"), Null)
        val (state, commands) = updateForNotification(message, initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoWarnUnacceptableMessage(message, initialState))
    }

}
