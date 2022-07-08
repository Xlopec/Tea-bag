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

package io.github.xlopec.tea.time.travel.plugin.feature.component.integration

import io.github.xlopec.tea.time.travel.plugin.data.*
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.server.DoApplyState
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.*

@RunWith(JUnit4::class)
internal class UpdateForUiMessageTest {

    @Test
    fun `test when remove snapshot by id and plugin state is Started then snapshot gets removed`() {

        val snapshotId = RandomSnapshotId()
        val meta = SnapshotMeta(snapshotId, TestTimestamp1)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId -> NonEmptyComponentDebugState(ComponentId(strId), meta) }
        val pluginState =
            StartedFromPairs(ValidTestSettings, otherStates + NonEmptyComponentDebugState(id, meta))

        RemoveSnapshots(id, snapshotId)
        val (state, commands) = pluginState.onUpdateForComponentMessage(RemoveSnapshots(id, snapshotId))

        assertTrue(commands.isEmpty())
        assertEquals(StartedFromPairs(ValidTestSettings, otherStates + (id to DebuggableComponent(id, Null))), state)
    }

    @Test
    fun `test when remove snapshot by ids and plugin state is Started then snapshot gets removed`() {

        val iterations = 100
        val hi = 50
        val meta = iterations.times { RandomSnapshotId() }.map { id -> SnapshotMeta(id, TestTimestamp1) }

        val resultingOriginalSnapshots = meta.takeLast(iterations - hi).map(::EmptyOriginalSnapshot)
        val resultingFilteredSnapshots = meta.takeLast(iterations - hi).map(::EmptyFilteredSnapshot)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId ->
            NonEmptyComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp1))
        }

        val pluginState = StartedFromPairs(
            ValidTestSettings,
            otherStates + ComponentDebugState(
                id,
                (meta.take(hi).map(::EmptyOriginalSnapshot) + resultingOriginalSnapshots).toPersistentList(),
                (meta.take(hi).map(::EmptyFilteredSnapshot) + resultingFilteredSnapshots).toPersistentList()
            )
        )

        RemoveSnapshots(id, meta.take(hi).map { (id, _) -> id }.toSet())
        val (state, commands) = pluginState.onUpdateForComponentMessage(
            RemoveSnapshots(id, meta.take(hi).map { (id, _) -> id }.toSet())
        )

        assertTrue(commands.isEmpty())
        assertEquals(
            StartedFromPairs(
                ValidTestSettings,
                otherStates + ComponentDebugState(
                    id,
                    resultingOriginalSnapshots.toPersistentList(),
                    resultingFilteredSnapshots.toPersistentList()
                )
            ), state
        )
    }

    @Test
    fun `test when remove all snapshots and plugin state is Started then all snapshots for the component are removed`() {

        val snapshotId = RandomSnapshotId()
        val meta = SnapshotMeta(snapshotId, TestTimestamp1)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId -> NonEmptyComponentDebugState(ComponentId(strId), meta) }
        val pluginState =
            StartedFromPairs(ValidTestSettings, otherStates + NonEmptyComponentDebugState(id, meta))

        val (state, commands) = pluginState.onUpdateForComponentMessage(RemoveAllSnapshots(id))

        assertTrue(commands.isEmpty())
        assertEquals(StartedFromPairs(ValidTestSettings, otherStates + (id to DebuggableComponent(id, Null))), state)
    }

    @Test
    fun `test when remove component and plugin state is Started then that component is removed`() {

        val removalComponentId = ComponentId("a")
        val otherStates = ComponentDebugStates()

        val initialState = StartedFromPairs(
            ValidTestSettings,
            otherStates + NonEmptyComponentDebugState(
                removalComponentId,
                SnapshotMeta(RandomSnapshotId(), TestTimestamp1)
            )
        )

        RemoveComponent(removalComponentId)
        val (state, commands) = initialState.onUpdateForComponentMessage(RemoveComponent(removalComponentId))

        assertTrue(commands.isEmpty())
        assertEquals(StartedFromPairs(ValidTestSettings, otherStates), state)
    }

    @Test
    fun `test when apply message and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp1)

        val initialState = StartedFromPairs(
            ValidTestSettings,
            ComponentDebugState(
                componentId,
                persistentListOf(OriginalSnapshot(meta, value, Null, CollectionWrapper(listOf()))),
                persistentListOf(FilteredSnapshot(meta, Null))
            )
        )

        ApplyMessage(componentId, snapshotId)
        val (state, commands) = initialState.onUpdateForComponentMessage(ApplyMessage(componentId, snapshotId))

        assertSame(initialState, state)
        assertEquals(setOf(DoApplyMessage(componentId, value, StartedTestServerStub)), commands)
    }

    @Test
    fun `test when apply state and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp1)

        val initialState = StartedFromPairs(
            ValidTestSettings,
            ComponentDebugState(
                componentId,
                persistentListOf(OriginalSnapshot(meta, Null, value, CollectionWrapper(listOf()))),
                persistentListOf(FilteredSnapshot(meta, value))
            )
        )

        ApplyState(componentId, snapshotId)
        val (state, commands) = initialState.onUpdateForComponentMessage(ApplyState(componentId, snapshotId))

        assertSame(initialState, state)
        assertEquals(setOf(DoApplyState(componentId, value, StartedTestServerStub)), commands)
    }

    @Test
    fun `test when update filter with empty substring and SUBSTRING option and plugin state is Started then it's updated properly`() {

        val componentId = ComponentId("a")
        val initialState =
            StartedFromPairs(ValidTestSettings, NonEmptyComponentDebugState(componentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp1)))

        UpdateFilter(
            ComponentId("a"),
            "",
            ignoreCase = false,
            option = FilterOption.SUBSTRING
        )
        val (state, commands) = initialState.onUpdateForComponentMessage(
            UpdateFilter(
                ComponentId("a"),
                "",
                ignoreCase = false,
                option = FilterOption.SUBSTRING
            )
        )

        assertTrue(commands.isEmpty())
        assertIs<Server>(state.server)

        with(state.debugger.component(componentId)) {
            assertFalse(filter.ignoreCase)
            assertSame(FilterOption.SUBSTRING, filter.option)
            assertIs<Valid<String>>(filter.predicate)
            assertEquals("", filter.predicate.input)

            assertEquals(snapshots.size, filteredSnapshots.size)

            snapshots.indices.forEach { i ->
                val s = snapshots[i]
                val fs = filteredSnapshots[i]

                assertEquals(s.state, fs.state)
                assertEquals(s.message, fs.message)
                assertEquals(s.meta, fs.meta)
            }
        }
    }

    @Test
    fun `test when update filter with non empty substring and SUBSTRING option and plugin state is Started then it's updated properly`() {

        val componentId = ComponentId("a")
        val input = "abc"
        val initialState =
            StartedFromPairs(ValidTestSettings, NonEmptyComponentDebugState(componentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp1)))

        UpdateFilter(
            ComponentId("a"),
            input,
            ignoreCase = false,
            option = FilterOption.SUBSTRING
        )
        val (state, commands) = initialState.onUpdateForComponentMessage(
            UpdateFilter(
                ComponentId("a"),
                input,
                ignoreCase = false,
                option = FilterOption.SUBSTRING
            )
        )

        assertTrue(commands.isEmpty())
        assertIs<Server>(state.server)

        val component = state.debugger.component(componentId)

        assertFalse(component.filter.ignoreCase)
        assertSame(FilterOption.SUBSTRING, component.filter.option)

        assertNotNull(component.filter.predicate)
        assertIs<Valid<String>>(component.filter.predicate)
    }
}
