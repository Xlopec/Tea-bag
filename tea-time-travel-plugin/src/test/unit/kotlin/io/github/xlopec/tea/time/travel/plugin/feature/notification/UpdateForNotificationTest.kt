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
import io.github.xlopec.tea.time.travel.plugin.integration.FileException
import io.github.xlopec.tea.time.travel.plugin.integration.NotificationMessage
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@RunWith(JUnit4::class)
internal class UpdateForNotificationTest {

    @get:Rule
    internal var fileRule = TemporaryFolder()

    @Test
    fun `when message is NotifyStarted then plugin goes to a Started state`() {
        val (nextState, commands) = State(ValidTestSettings).updateForNotificationMessage(ServerStarted(StartedTestServerStub))

        assertEquals(
            State(
                debugger = Debugger(settings = ValidTestSettings),
                server = StartedTestServerStub,
            ), nextState
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = State(
            debugger = Debugger(settings = ValidTestSettings),
            server = StartedTestServerStub,
        ).updateForNotificationMessage(ServerStopped)

        assertEquals(State(ValidTestSettings), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when append snapshot to non-existing component then it gets appended`() {
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val commandsWrapper = CollectionWrapper(listOf(message))
        val otherStates = ComponentDebugStates('b'..'z')
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)

        val (nextState, commands) = StartedFromPairs(settings = ValidTestSettings, states = otherStates).updateForNotificationMessage(
            AppendSnapshot(componentId, meta, message, oldState, newState, commandsWrapper)
        )

        val expectedDebugState = DebuggableComponent(
            id = componentId,
            state = newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState, commandsWrapper))
        )

        assertEquals(StartedFromPairs(settings = ValidTestSettings, states = otherStates + (componentId to expectedDebugState)), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when append snapshot to existing component then it gets appended`() {
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

        val (nextState, commands) = StartedFromPairs(settings = ValidTestSettings, states = otherStates).updateForNotificationMessage(
            AppendSnapshot(componentId, meta, message, oldState, newState, commandsWrapper)
        )

        val expectedDebugState = DebuggableComponent(
            id = componentId,
            state = newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState))
        )

        assertEquals(StartedFromPairs(settings = ValidTestSettings, states = otherStates + (componentId to expectedDebugState)), nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when append snapshot to component and snapshots number exceeds max retained number, then first snapshot is removed`() {
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val commandsWrapper = CollectionWrapper(listOf())

        val settings = ValidTestSettings.copy(maxSnapshots = 1.toPInt())
        val (nextState, commands) = StartedFromPairs(settings, ComponentDebugState(componentId))
            .updateForNotificationMessage(AppendSnapshot(componentId, meta, message, oldState, newState, commandsWrapper))

        val expectedDebugState = DebuggableComponent(
            id = componentId,
            state = newState,
            snapshots = persistentListOf(OriginalSnapshot(meta, message, newState, commandsWrapper)),
            filteredSnapshots = persistentListOf(FilteredSnapshot(meta, message, newState))
        )

        assertEquals(
            StartedFromPairs(settings, componentId to expectedDebugState),
            nextState
        )

        assertEquals(setOf(), commands)
    }

    @Test
    fun `when apply state then it gets applied`() {
        val otherStates = ComponentDebugStates('a'..'z') { strId ->
            val id = ComponentId(strId)

            if (id.value == "a") ComponentDebugState(id)
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp1))
        }

        val componentId = ComponentId("a")
        val newState = StringWrapper("d")

        val (nextState, commands) = StartedFromPairs(
            settings = ValidTestSettings,
            activeComponent = componentId,
            states = otherStates
        ).updateForNotificationMessage(StateDeployed(componentId, newState))

        val expectedDebugState = DebuggableComponent(componentId, newState)

        assertEquals(
            StartedFromPairs(
                settings = ValidTestSettings,
                activeComponent = componentId,
                states = otherStates.takeLast(otherStates.size - 1) + (componentId to expectedDebugState)
            ),
            nextState
        )
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when apply state and component does not exist then it does not get applied`() {
        val state = StartedFromPairs(settings = ValidTestSettings, states = ComponentDebugStates())

        val (nextState, commands) = state.updateForNotificationMessage(StateDeployed(ComponentId("a"), StringWrapper("d")))

        assertSame(state, nextState)
        assertTrue(commands.isEmpty())
    }

    @Test
    fun `when append new component then it gets appended`() {
        val otherStates = ComponentDebugStates { strId -> ComponentDebugState(ComponentId(strId)) }
        val componentId = ComponentId("a")
        val state = StringWrapper("d")
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val collectionWrapper = CollectionWrapper(listOf(StringWrapper("a")))

        val (nextState, commands) = StartedFromPairs(settings = ValidTestSettings, states = otherStates).updateForNotificationMessage(
            ComponentAttached(
                id = componentId,
                meta = meta,
                state = state,
                commands = collectionWrapper
            )
        )

        assertEquals(
            StartedFromPairs(
                settings = ValidTestSettings,
                activeComponent = componentId,
                states = otherStates +
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

        assertEquals(setOf(DoNotifyComponentAttached(id = componentId, isComponentReattached = false)), commands)
    }

    @Test
    fun `when component import session and operation fails, then DoNotifyFileOperationFailure is produced`() {
        val file = fileRule.newFile()
        val fileException = FileException("import failure", FileNotFoundException("can't read file"), file)
        val (state, commands) = StartedFromPairs(ValidTestSettings).updateForNotificationMessage(ComponentImportFailure(fileException))

        assertEquals(StartedFromPairs(ValidTestSettings), state)
        assertEquals(
            setOf(
                DoNotifyFileOperationFailure(
                    "Import failure",
                    formatExceptionDescription("Couldn't import session", fileException, ". Check if file is valid"),
                    file
                )
            ),
            commands
        )
    }

    @Test
    fun `when component is imported, then it's appended and DoNotifyFileOperationSuccess is produced`() {
        val id = ComponentId("a")
        val componentState = StringWrapper("abc")
        val importedFrom = fileRule.newFile()

        val (state, commands) = StartedFromPairs(ValidTestSettings).updateForNotificationMessage(
            ComponentImportSuccess(
                from = importedFrom,
                sessionState = DebuggableComponent(
                    id = id,
                    state = componentState
                )
            )
        )

        assertEquals(
            StartedFromPairs(
                ValidTestSettings,
                ComponentDebugState(
                    componentId = id,
                    state = componentState,
                )
            ),
            state
        )

        assertEquals(
            setOf(
                DoNotifyFileOperationSuccess(
                    title = "Import success",
                    description = "Session \"${id.value}\" were imported",
                    forFile = importedFrom
                )
            ),
            commands
        )
    }

    @Test
    fun `when component export session and operation fails, then DoNotifyFileOperationFailure is produced`() {
        val id = ComponentId("component")
        val file = fileRule.newFile()
        val fileException = FileException("export failure", FileNotFoundException("no write permission"), file)
        val (state, commands) = StartedFromPairs(ValidTestSettings).updateForNotificationMessage(ComponentExportFailure(id, fileException))

        assertEquals(StartedFromPairs(ValidTestSettings), state)
        assertEquals(
            setOf(
                DoNotifyFileOperationFailure(
                    "Export failure",
                    formatExceptionDescription("Failed to export \"${id.value}\"", fileException),
                    file
                )
            ),
            commands
        )
    }

    @Test
    fun `when component export session, then DoNotifyFileOperationSuccess is produced`() {
        val id = ComponentId("component")
        val file = fileRule.newFile()
        val (state, commands) = StartedFromPairs(ValidTestSettings).updateForNotificationMessage(ComponentExportSuccess(id, file))

        assertEquals(StartedFromPairs(ValidTestSettings), state)
        assertEquals(
            setOf(
                DoNotifyFileOperationSuccess(
                    "Export success",
                    "Session \"${id.value}\" were exported",
                    file
                )
            ),
            commands
        )
    }

    @Test
    fun `when component is imported, then it's rewritten for same session and DoNotifyComponentImported is produced`() {
        val id = ComponentId("a")
        val componentState = StringWrapper("abc")
        val importedFrom = fileRule.newFile()

        val (state, commands) = StartedFromPairs(
            ValidTestSettings,
            ComponentDebugState(
                componentId = id,
                state = NumberWrapper(10),
            )
        ).updateForNotificationMessage(
            ComponentImportSuccess(
                from = importedFrom,
                sessionState = DebuggableComponent(
                    id = id,
                    state = componentState
                )
            )
        )

        assertEquals(
            StartedFromPairs(
                ValidTestSettings,
                ComponentDebugState(
                    componentId = id,
                    state = componentState,
                )
            ),
            state
        )

        assertEquals(
            setOf(
                DoNotifyFileOperationSuccess(
                    title = "Import success",
                    description = "Session \"${id.value}\" were imported",
                    forFile = importedFrom
                )
            ), commands
        )
    }

    @Test
    fun `when append component twice given clearSnapshotsOnAttach option enabled, then it's appended only once`() {
        val componentId = ComponentId("a")
        val state = StringWrapper("d")
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)

        val message = ComponentAttached(id = componentId, meta = meta, state = state, commands = CollectionWrapper())
        val settings = ValidTestSettings.copy(clearSnapshotsOnAttach = true)
        val (state1, commands1) = StartedFromPairs(settings).updateForNotificationMessage(message)
        val (state2, commands2) = state1.updateForNotificationMessage(message)

        val expectedState = StartedFromPairs(
            settings,
            ComponentDebugState(
                componentId = componentId,
                state = state,
                snapshots = persistentListOf(
                    OriginalSnapshot(
                        meta = meta,
                        message = null,
                        state = state,
                        commands = CollectionWrapper()
                    )
                ),
                filteredSnapshots = persistentListOf(
                    FilteredSnapshot(
                        meta = meta,
                        message = null,
                        state = state,
                        commands = null
                    )
                )
            )
        )

        assertEquals(expectedState, state1)
        assertEquals(expectedState, state2)

        assertEquals(setOf(DoNotifyComponentAttached(id = componentId, isComponentReattached = false)), commands1)
        assertEquals(setOf(DoNotifyComponentAttached(id = componentId, isComponentReattached = true)), commands2)
    }

    @Test
    fun `when append component twice given clearSnapshotsOnAttach option disabled, then it's appended twice`() {
        val componentId = ComponentId("a")
        val state = StringWrapper("d")
        val meta = SnapshotMeta(TestSnapshotId1, TestTimestamp1)

        val message = ComponentAttached(id = componentId, meta = meta, state = state, commands = CollectionWrapper())
        val settings = ValidTestSettings.copy(clearSnapshotsOnAttach = false)
        val (state1, commands1) = StartedFromPairs(settings).updateForNotificationMessage(message)

        assertEquals(
            StartedFromPairs(
                settings,
                ComponentDebugState(
                    componentId = componentId,
                    state = state,
                    snapshots = persistentListOf(
                        OriginalSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = CollectionWrapper()
                        )
                    ),
                    filteredSnapshots = persistentListOf(
                        FilteredSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = null
                        )
                    )
                )
            ), state1
        )

        val (state2, commands2) = state1.updateForNotificationMessage(message)

        assertEquals(
            StartedFromPairs(
                settings,
                ComponentDebugState(
                    componentId = componentId,
                    state = state,
                    snapshots = persistentListOf(
                        OriginalSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = CollectionWrapper()
                        ),
                        OriginalSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = CollectionWrapper()
                        )
                    ),
                    filteredSnapshots = persistentListOf(
                        FilteredSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = null
                        ),
                        FilteredSnapshot(
                            meta = meta,
                            message = null,
                            state = state,
                            commands = null
                        )
                    )
                )
            ), state2
        )

        assertEquals(setOf(DoNotifyComponentAttached(id = componentId, isComponentReattached = false)), commands1)
        assertEquals(setOf(DoNotifyComponentAttached(id = componentId, isComponentReattached = true)), commands2)
    }

    @Test
    fun `when illegal combination of message and state warning command is returned`() {
        val initialState = State(ValidTestSettings)
        val message = object : NotificationMessage {}
        val (state, commands) = initialState.updateForNotificationMessage(message)

        assertSame(initialState, state)
        assertEquals(setOf(DoWarnUnacceptableMessage(message, initialState)), commands)
    }
}
