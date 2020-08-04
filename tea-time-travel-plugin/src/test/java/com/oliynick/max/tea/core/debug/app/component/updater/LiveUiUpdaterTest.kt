@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDateTime
import java.util.*

private val TestTimestamp: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1)

@RunWith(JUnit4::class)
internal class LiveUiUpdaterTest {

    private val updater: Updater<UIMessage, PluginState, PluginCommand> = LiveUiUpdater::update

    @Test
    fun `test the result is calculated properly given plugin state is Stopped and message is StartServer`() =
        forAll(SettingsGen) { settings ->

            val stopped = Stopped(settings, StoppedTestServer)
            val (state, commands) = updater(StartServer, stopped)

            val startCommand by lazy {
                DoStartServer(ServerAddress(settings.host.value!!, settings.port.value!!), StoppedTestServer)
            }

            if (settings.host.isValid() && settings.port.isValid()) {
                state == Starting(settings) && commands == setOf(startCommand)
            } else {
                state === stopped && commands.isEmpty()
            }
        }

    @Test
    fun `test the result is calculated properly given plugin state is Started and message is StopServer`() =
        forAll(SettingsGen) { settings ->

            val pluginState = Started(settings, DebugState(), StartedTestServer())
            val (state, commands) = updater(StopServer, pluginState)

            state == Stopping(settings) && commands == setOf(DoStopServer(pluginState.server))
        }

    @Test
    fun `test when remove snapshot by id and plugin state is Started then snapshot gets removed`() {

        val snapshotId = RandomSnapshotId()
        val meta = SnapshotMeta(snapshotId, TestTimestamp)
        val id = ComponentId("a")

        val otherStates = testComponentDebugStates { strId -> TestComponentDebugState(ComponentId(strId), meta) }
        val pluginState = StartedDebugState(otherStates + TestComponentDebugState(id, meta))

        val (state, commands) = updater(RemoveSnapshots(id, snapshotId), pluginState)

        commands.shouldBeEmpty()
        state shouldBe StartedDebugState(otherStates + (id to ComponentDebugState(id, Null)))
    }

    @Test
    fun `test when remove snapshot by ids and plugin state is Started then snapshot gets removed`() {

        val iterations = 100
        val hi = 50
        val meta = iterations.times { RandomSnapshotId() }.map { id -> SnapshotMeta(id, TestTimestamp) }

        val resultingOriginalSnapshots = meta.takeLast(iterations - hi).map(::EmptyOriginalSnapshot)
        val resultingFilteredSnapshots = meta.takeLast(iterations - hi).map(::EmptyFilteredSnapshot)
        val id = ComponentId("a")

        val otherStates = testComponentDebugStates { strId ->
            TestComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        }

        val pluginState = StartedDebugState(
                otherStates + TestComponentDebugState(
                        id,
                        (meta.take(hi).map(::EmptyOriginalSnapshot) + resultingOriginalSnapshots).toPersistentList(),
                        (meta.take(hi).map(::EmptyFilteredSnapshot) + resultingFilteredSnapshots).toPersistentList()
                )
        )

        val (state, commands) = updater(RemoveSnapshots(id, meta.take(hi).map { (id, _) -> id }.toSet()), pluginState)

        commands.shouldBeEmpty()
        state shouldBe StartedDebugState(
                otherStates + TestComponentDebugState(
                        id,
                        resultingOriginalSnapshots.toPersistentList(),
                        resultingFilteredSnapshots.toPersistentList()
                )
        )
    }

    @Test
    fun `test when apply command and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp)

        val initialState = StartedDebugState(
                TestComponentDebugState(
                        componentId,
                        persistentListOf(OriginalSnapshot(meta, value, Null)),
                        persistentListOf(FilteredSnapshot.ofState(meta, Null))
                )
        )

        val (state, commands) = updater(ApplyMessage(componentId, snapshotId), initialState)

        state shouldBeSameInstanceAs initialState
        commands shouldContainExactly setOf(DoApplyMessage(componentId, value, StartedServerStub))
    }

    @Test
    fun `test when apply state and plugin state is Started then a proper Value is found and correct command is returned`() {

        val componentId = ComponentId("a")
        val snapshotId = RandomSnapshotId()
        val value = StringWrapper("some value")
        val meta = SnapshotMeta(snapshotId, TestTimestamp)

        val initialState = StartedDebugState(
                TestComponentDebugState(
                        componentId,
                        persistentListOf(OriginalSnapshot(meta, Null, value)),
                        persistentListOf(FilteredSnapshot.ofState(meta, value))
                )
        )

        val (state, commands) = updater(ApplyState(componentId, snapshotId), initialState)

        state shouldBeSameInstanceAs initialState
        commands shouldContainExactly setOf(DoApplyState(componentId, value, StartedServerStub))
    }

    @Test
    fun `test when remove component and plugin state is Started then that component is removed`() {

        val removalComponentId = ComponentId("a")
        val otherStates = testComponentDebugStates()

        val initialState = StartedDebugState(
                otherStates + TestComponentDebugState(removalComponentId, SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        )

        val (state, commands) = updater(RemoveComponent(removalComponentId), initialState)

        commands.shouldBeEmpty()
        state shouldBe StartedDebugState(otherStates)
    }

}

private inline fun testComponentDebugStates(
    range: CharRange = 'b'..'z',
    block: (strId: String) -> Pair<ComponentId, ComponentDebugState> = { strId ->
        TestComponentDebugState(ComponentId(strId), SnapshotMeta(RandomSnapshotId(), TestTimestamp))
    }
) = range
    .map { it.toString() }
    .map(block)

private fun EmptyOriginalSnapshot(
    m: SnapshotMeta
) = OriginalSnapshot(m, Null, Null)

private fun EmptyFilteredSnapshot(
    m: SnapshotMeta
) = FilteredSnapshot.ofBoth(m, Null, Null)

private fun TestComponentDebugState(
    componentId: ComponentId,
    meta: SnapshotMeta
) = TestComponentDebugState(
        componentId,
        persistentListOf(OriginalSnapshot(meta, Null, Null)),
        persistentListOf(FilteredSnapshot.ofBoth(meta, Null, Null))
)

private fun TestComponentDebugState(
    componentId: ComponentId,
    snapshots: PersistentList<OriginalSnapshot>,
    filteredSnapshots: PersistentList<FilteredSnapshot>
) = componentId to ComponentDebugState(componentId, Null, snapshots = snapshots, filteredSnapshots = filteredSnapshots)

private fun StartedDebugState(
    states: Iterable<Pair<ComponentId, ComponentDebugState>>
) = Started(
        Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false),
        DebugState(states.toMap().toPersistentMap()),
        StartedServerStub
)

private fun StartedDebugState(
    vararg states: Pair<ComponentId, ComponentDebugState>
) = Started(
        Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false),
        DebugState(states.toMap().toPersistentMap()),
        StartedServerStub
)

private fun RandomSnapshotId() = SnapshotId(UUID.randomUUID())
