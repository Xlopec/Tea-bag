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

package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.debug.app.component.cms.command.Command
import com.oliynick.max.tea.core.debug.app.component.cms.command.DoNotifyComponentAttached
import com.oliynick.max.tea.core.debug.app.component.cms.command.DoWarnUnacceptableMessage
import com.oliynick.max.tea.core.debug.app.component.cms.message.*
import com.oliynick.max.tea.core.debug.app.component.cms.state.State
import com.oliynick.max.tea.core.debug.app.component.cms.state.Stopped
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class LiveNotificationUpdaterTest {

    private val updater: Updater<NotificationMessage, State, Command> = LiveNotificationUpdater::update

    @Test
    fun `test when message is NotifyStarted then plugin goes to a Started state`() {

        val (nextState, commands) = updater(NotifyStarted(StartedTestServerStub), Stopped(TestSettings))

        nextState shouldBe com.oliynick.max.tea.core.debug.app.component.cms.state.Started(
            TestSettings,
            DebugState(),
            StartedTestServerStub
        )
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = updater(
            NotifyStopped,
            com.oliynick.max.tea.core.debug.app.component.cms.state.Started(
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
        val meta = SnapshotMeta(TestSnapshotId, TestTimestamp)

        val (nextState, commands) = updater(
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
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        }

        val meta = SnapshotMeta(TestSnapshotId, TestTimestamp)
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val commandsWrapper = CollectionWrapper(listOf())

        val (nextState, commands) = updater(
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
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        }

        val componentId = ComponentId("a")
        val newState = StringWrapper("d")

        val (nextState, commands) = updater(
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

        val (nextState, commands) = updater(StateApplied(ComponentId("a"), StringWrapper("d")), state)

        nextState shouldBeSameInstanceAs state
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when append new component then it gets appended`() {

        val otherStates = ComponentDebugStates { strId -> ComponentDebugState(ComponentId(strId)) }
        val componentId = ComponentId("a")
        val state = StringWrapper("d")
        val meta = SnapshotMeta(TestSnapshotId, TestTimestamp)
        val collectionWrapper = CollectionWrapper(listOf(StringWrapper("a")))

        val (nextState, commands) = updater(
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
        val (state, commands) = updater(message, initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoWarnUnacceptableMessage(message, initialState))
    }

}
