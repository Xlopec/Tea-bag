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
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import core.misc.shouldForEach
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.*
import io.kotlintest.properties.forAll
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class LiveUiUpdaterTest {

    private val updater: Updater<UIMessage, PluginState, PluginCommand> = LiveUiUpdater::update

    @Test
    fun `test the result is calculated properly given plugin state is Stopped and message is StartServer`() =
        forAll(SettingsGen) { settings ->

            val stopped = Stopped(settings)
            val (state, commands) = updater(StartServer, stopped)

            if (settings.host.isValid() && settings.port.isValid()) {
                state == Starting(settings)
                        && commands == setOf(DoStartServer(ServerAddress(settings.host.value!!, settings.port.value!!)))
            } else {
                state === stopped && commands.isEmpty()
            }
        }

    @Test
    fun `test the result is calculated properly given plugin state is Started and message is StopServer`() =
        forAll(SettingsGen) { settings ->

            val pluginState = Started(settings, DebugState(), StartedTestServerStub)
            val (state, commands) = updater(StopServer, pluginState)

            state == Stopping(settings) && commands == setOf(DoStopServer(pluginState.server))
        }

    @Test
    fun `test when remove snapshot by id and plugin state is Started then snapshot gets removed`() {

        val snapshotId = RandomSnapshotId()
        val meta = SnapshotMeta(snapshotId, TestTimestamp)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId -> NonEmptyComponentDebugState(ComponentId(strId), meta) }
        val pluginState =
            Started(otherStates + NonEmptyComponentDebugState(id, meta))

        val (state, commands) = updater(RemoveSnapshots(id, snapshotId), pluginState)

        commands.shouldBeEmpty()
        state shouldBe Started(otherStates + (id to ComponentDebugState(id, Null)))
    }

    @Test
    fun `test when remove snapshot by ids and plugin state is Started then snapshot gets removed`() {

        val iterations = 100
        val hi = 50
        val meta = iterations.times { RandomSnapshotId() }.map { id -> SnapshotMeta(id, TestTimestamp) }

        val resultingOriginalSnapshots = meta.takeLast(iterations - hi).map(::EmptyOriginalSnapshot)
        val resultingFilteredSnapshots = meta.takeLast(iterations - hi).map(::EmptyFilteredSnapshot)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId ->
            NonEmptyComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        }

        val pluginState = Started(
                otherStates + ComponentDebugState(
                        id,
                        (meta.take(hi).map(::EmptyOriginalSnapshot) + resultingOriginalSnapshots).toPersistentList(),
                        (meta.take(hi).map(::EmptyFilteredSnapshot) + resultingFilteredSnapshots).toPersistentList()
                )
        )

        val (state, commands) = updater(RemoveSnapshots(id, meta.take(hi).map { (id, _) -> id }.toSet()), pluginState)

        commands.shouldBeEmpty()
        state shouldBe Started(
                otherStates + ComponentDebugState(
                        id,
                        resultingOriginalSnapshots.toPersistentList(),
                        resultingFilteredSnapshots.toPersistentList()
                )
        )
    }

    @Test
    fun `test when remove all snapshots and plugin state is Started then all snapshots for the component are removed`() {

        val snapshotId = RandomSnapshotId()
        val meta = SnapshotMeta(snapshotId, TestTimestamp)
        val id = ComponentId("a")

        val otherStates = ComponentDebugStates { strId -> NonEmptyComponentDebugState(ComponentId(strId), meta) }
        val pluginState =
            Started(otherStates + NonEmptyComponentDebugState(id, meta))

        val (state, commands) = updater(RemoveAllSnapshots(id), pluginState)

        commands.shouldBeEmpty()
        state shouldBe Started(otherStates + (id to ComponentDebugState(id, Null)))

    }

    @Test
    fun `test when remove component and plugin state is Started then that component is removed`() {

        val removalComponentId = ComponentId("a")
        val otherStates = ComponentDebugStates()

        val initialState = Started(
                otherStates + NonEmptyComponentDebugState(removalComponentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        )

        val (state, commands) = updater(RemoveComponent(removalComponentId), initialState)

        commands.shouldBeEmpty()
        state shouldBe Started(otherStates)
    }

    @Test
    fun `test when apply message and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp)

        val initialState = Started(
                ComponentDebugState(
                        componentId,
                        persistentListOf(OriginalSnapshot(meta, value, Null)),
                        persistentListOf(FilteredSnapshot.ofState(meta, Null))
                )
        )

        val (state, commands) = updater(ApplyMessage(componentId, snapshotId), initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoApplyMessage(componentId, value, StartedTestServerStub))
    }

    @Test
    fun `test when apply state and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp)

        val initialState = Started(
                ComponentDebugState(
                        componentId,
                        persistentListOf(OriginalSnapshot(meta, Null, value)),
                        persistentListOf(FilteredSnapshot.ofState(meta, value))
                )
        )

        val (state, commands) = updater(ApplyState(componentId, snapshotId), initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoApplyState(componentId, value, StartedTestServerStub))
    }

    @Test
    fun `test when update filter with empty substring and SUBSTRING option and plugin state is Started then it's updated properly`() {

        val componentId = ComponentId("a")
        val initialState =
            Started(NonEmptyComponentDebugState(componentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp)))

        val (state, commands) = updater(
                UpdateFilter(
                        ComponentId("a"),
                        "",
                        ignoreCase = false,
                        option = FilterOption.SUBSTRING
                ),
                initialState
        )

        commands.shouldBeEmpty()
        state.shouldBeInstanceOf<Started>()

        with((state as Started).debugState.component(componentId)) {
            filter.should { filter ->
                filter.ignoreCase.shouldBeFalse()
                filter.option shouldBeSameInstanceAs FilterOption.SUBSTRING
                filter.predicate.shouldBeNull()
            }

            snapshots.shouldForEach(filteredSnapshots) { s, fs ->
                fs.state shouldBe s.state
                fs.message shouldBe s.message
                fs.meta shouldBe s.meta
            }
        }
    }

    @Test
    fun `test when update filter with non empty substring and SUBSTRING option and plugin state is Started then it's updated properly`() {

        val componentId = ComponentId("a")
        val input = "abc"
        val initialState =
            Started(NonEmptyComponentDebugState(componentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp)))

        val (state, commands) = updater(
                UpdateFilter(
                        ComponentId("a"),
                        input,
                        ignoreCase = false,
                        option = FilterOption.SUBSTRING
                ),
                initialState
        )

        commands.shouldBeEmpty()
        state.shouldBeInstanceOf<Started>()

        val component = (state as Started).debugState.component(componentId)

        component.filter.should { filter ->
            filter.ignoreCase.shouldBeFalse()
            filter.option shouldBeSameInstanceAs FilterOption.SUBSTRING

            filter.predicate.should { predicate ->
                predicate.shouldNotBeNull()
                predicate.shouldBeInstanceOf<Valid<String>>()
            }
        }
    }

    @Test
    fun `test when illegal combination of message and state warning command is returned`() {

        val initialState = Stopped(TestSettings)
        val (state, commands) = updater(StopServer, initialState)

        state shouldBeSameInstanceAs initialState
        commands.shouldContainExactly(DoWarnUnacceptableMessage(StopServer, initialState))
    }

}
