@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.*
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LiveNotificationUpdaterTest {

    private val updater: Updater<NotificationMessage, PluginState, PluginCommand> = LiveNotificationUpdater::update

    @Test
    fun `test when message is NotifyStarted then plugin goes to a Started state`() {

        val (nextState, commands) = updater(NotifyStarted(StartedTestServerStub), Stopped(TestSettings))

        nextState shouldBe Started(TestSettings, DebugState(), StartedTestServerStub)
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = updater(NotifyStopped, Started(TestSettings, DebugState(), StartedTestServerStub))

        nextState shouldBe Stopped(TestSettings)
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when append snapshot to non-existing component then it gets appended`() {

        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")
        val otherStates = TestComponentDebugStates('b'..'z')
        val meta = SnapshotMeta(TestSnapshotId, TestTimestamp)

        val (nextState, commands) = updater(
                AppendSnapshot(componentId, meta, message, oldState, newState),
                TestStartedState(otherStates)
        )

        val expectedDebugState = ComponentDebugState(
                componentId,
                newState,
                snapshots = persistentListOf(OriginalSnapshot(meta, message, newState)),
                filteredSnapshots = persistentListOf(FilteredSnapshot.ofBoth(meta, message, newState))
        )

        nextState shouldBe TestStartedState(otherStates + (componentId to expectedDebugState))
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when append snapshot to existing component then it gets appended`() {

        val otherStates = TestComponentDebugStates('a'..'z') { strId ->

            val id = ComponentId(strId)

            if (id.value == "a") EmptyComponentDebugState(id)
            else NonEmptyComponentDebugState(id, SnapshotMeta(RandomSnapshotId(), TestTimestamp))
        }

        val meta = SnapshotMeta(TestSnapshotId, TestTimestamp)
        val componentId = ComponentId("a")
        val message = StringWrapper("b")
        val oldState = StringWrapper("c")
        val newState = StringWrapper("d")

        val (nextState, commands) = updater(
                AppendSnapshot(componentId, meta, message, oldState, newState),
                TestStartedState(otherStates)
        )

        val expectedDebugState = ComponentDebugState(
                componentId,
                newState,
                snapshots = persistentListOf(OriginalSnapshot(meta, message, newState)),
                filteredSnapshots = persistentListOf(FilteredSnapshot.ofBoth(meta, message, newState))
        )

        nextState shouldBe TestStartedState(otherStates + (componentId to expectedDebugState))
        commands.shouldBeEmpty()
    }

}