@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDateTime
import java.util.*

@RunWith(JUnit4::class)
internal class LiveUiUpdaterTest {

    private companion object {
        val TestTimestamp: LocalDateTime = LocalDateTime.of(2000, 1, 1, 1, 1)
    }

    private val updater: Updater<UIMessage, PluginState, PluginCommand> = LiveUiUpdater::update

    @Test
    fun `test the result is calculated properly given state is Stopped and message is StartServer`() =
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
    fun `test the result is calculated properly given state is Started and message is StopServer`() =
        forAll(SettingsGen) { settings ->

            val started = Started(settings, DebugState(), StartedTestServer())
            val (state, commands) = updater(StopServer, started)

            state == Stopping(settings) && commands == setOf(DoStopServer(started.server))
        }

    @Test
    fun `test when remove snapshot by id and state is Started then snapshot gets removed`() {

        val snapshotId = SnapshotId(UUID.randomUUID())
        val meta = SnapshotMeta(snapshotId, TestTimestamp)

        val data = ('b'..'z')
            .map { it.toString() }
            .map { componentId -> TestComponentDebugState(componentId, meta) }

        val started = StartedDebugState(data + TestComponentDebugState("a", meta))

        val (state, commands) = updater(RemoveSnapshots(ComponentId("a"), snapshotId), started)

        commands.shouldBeEmpty()
        state shouldBe StartedDebugState(data + TestEmptyComponentDebugState("a"))
    }

    @Test
    fun `test when remove snapshot by ids and state is Started then snapshot gets removed`() {

        val data = ('b'..'z')
            .map { it.toString() }
            .map { componentId -> TestComponentDebugState(componentId, SnapshotMeta(SnapshotId(UUID.randomUUID()), TestTimestamp)) }

        val iterations = 100
        val hi = 50
        val meta = iterations.times { SnapshotId(UUID.randomUUID()) }.map { id -> SnapshotMeta(id, TestTimestamp) }

        val resultingOriginalSnapshots = meta.takeLast(iterations - hi).map(::EmptyOriginalSnapshot)
        val resultingFilteredSnapshots = meta.takeLast(iterations - hi).map(::EmptyFilteredSnapshot)

        val started = StartedDebugState(
                data + TestComponentDebugState(
                        "a",
                        (meta.take(hi).map(::EmptyOriginalSnapshot) + resultingOriginalSnapshots).toPersistentList(),
                        (meta.take(hi).map(::EmptyFilteredSnapshot) + resultingFilteredSnapshots).toPersistentList()
                )
        )

        val (state, commands) = updater(
                RemoveSnapshots(
                        ComponentId("a"),
                        meta.take(hi).map { (id, _) -> id }.toSet()
                ),
                started
        )

        commands.shouldBeEmpty()
        state shouldBe StartedDebugState(
                data + TestComponentDebugState(
                        "a",
                        resultingOriginalSnapshots.toPersistentList(),
                        resultingFilteredSnapshots.toPersistentList()
                )
        )
    }

}

private fun EmptyOriginalSnapshot(
    m: SnapshotMeta
) = OriginalSnapshot(m, Null, Null)

private fun EmptyFilteredSnapshot(
    m: SnapshotMeta
) = FilteredSnapshot.ofBoth(m, Null, Null)

private fun TestEmptyComponentDebugState(
    componentId: String
) = ComponentId(componentId).let { id -> id to ComponentDebugState(id, Null) }

private fun TestComponentDebugState(
    componentId: String,
    meta: SnapshotMeta
) = TestComponentDebugState(
        componentId,
        persistentListOf(OriginalSnapshot(meta, Null, Null)),
        persistentListOf(FilteredSnapshot.ofBoth(meta, Null, Null))
)

private fun TestComponentDebugState(
    componentId: String,
    snapshots: PersistentList<OriginalSnapshot>,
    filteredSnapshots: PersistentList<FilteredSnapshot>
) = ComponentId(componentId).let { id ->
    id to ComponentDebugState(
            id,
            Null,
            snapshots = snapshots,
            filteredSnapshots = filteredSnapshots
    )
}

private fun StartedDebugState(
    states: Iterable<Pair<ComponentId, ComponentDebugState>>
) = Started(
        Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false),
        DebugState(states.toMap().toPersistentMap()),
        StartedServerStub
)
