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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.time.LocalDateTime
import java.util.*

@RunWith(JUnit4::class)
internal class LiveUiUpdaterTest {

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
    fun `test when remove snapshot by ids and state is Started snapshots are removed`() {

        val localTime = LocalDateTime.of(2000, 1, 1, 1, 1)
        val snapshotId = SnapshotId(UUID.randomUUID())

        //todo implement some test data sequence generator
        fun TestPair(
            value: String
        ) = ComponentId(value).let { id ->
            id to ComponentDebugState(
                    id, Null,
                    snapshots = persistentListOf(
                            OriginalSnapshot(
                                    SnapshotMeta(snapshotId, localTime),
                                    Null,
                                    Null
                            )
                    ),
                    filteredSnapshots = persistentListOf(
                            FilteredSnapshot.ofBoth(
                                    SnapshotMeta(snapshotId, localTime),
                                    Null,
                                    Null
                            )
                    )
            )
        }

        fun RandomPair(
            value: String
        ) = ComponentId(value).let { id ->

            val meta = SnapshotMeta(SnapshotId(UUID.randomUUID()), localTime)

            id to ComponentDebugState(
                    id,
                    Null,
                    snapshots = persistentListOf(OriginalSnapshot(meta, Null, Null)),
                    filteredSnapshots = persistentListOf(FilteredSnapshot.ofBoth(meta, Null, Null))
            )
        }

        fun EmptyStatePair(
            value: String
        ) = ComponentId(value).let { id -> id to ComponentDebugState(id, Null) }

        val data = ('b'..'z')
            .map { it.toString() }
            .map(::RandomPair)

        val started = SimpleDebugState(data + TestPair("a"))

        val (state, commands) = updater(RemoveSnapshots(ComponentId("a"), snapshotId), started)

        commands.shouldBeEmpty()
        state shouldBe SimpleDebugState(data + EmptyStatePair("a"))
    }

}


private fun SimpleDebugState(
    states: Iterable<Pair<ComponentId, ComponentDebugState>>
) = Started(
        Settings(Valid(TestHost.value, TestHost), Valid(TestPort.value.toString(), TestPort), false),
        DebugState(states.toMap().toPersistentMap()),
        StartedServerStub
)
