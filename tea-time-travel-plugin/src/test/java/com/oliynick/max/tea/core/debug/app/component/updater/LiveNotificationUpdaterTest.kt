package com.oliynick.max.tea.core.debug.app.component.updater

import com.oliynick.max.tea.core.component.Updater
import com.oliynick.max.tea.core.debug.app.component.cms.*
import com.oliynick.max.tea.core.debug.app.domain.DebugState
import com.oliynick.max.tea.core.debug.app.misc.*
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LiveNotificationUpdaterTest {

    private val updater: Updater<NotificationMessage, PluginState, PluginCommand> = LiveNotificationUpdater::update

    @Test
    fun `test when message is NotifyStarted then plugin goes to a Started state`() {

        val (nextState, commands) = updater(NotifyStarted(StartedTestServerStub), Stopped(TestSettings, StoppedTestServer))

        nextState shouldBe Started(TestSettings, DebugState(), StartedTestServerStub)
        commands.shouldBeEmpty()
    }

    @Test
    fun `test when message is NotifyStopped then plugin goes to a Stopped state`() {

        val (nextState, commands) = updater(NotifyStopped(StoppedTestServer), Started(TestSettings, DebugState(), StartedTestServerStub))

        nextState shouldBe Stopped(TestSettings, StoppedTestServer)
        commands.shouldBeEmpty()
    }

}