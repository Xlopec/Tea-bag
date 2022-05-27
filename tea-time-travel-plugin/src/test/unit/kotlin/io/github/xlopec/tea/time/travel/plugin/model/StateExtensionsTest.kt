package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugStates
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Test

class StateExtensionsTest {

    @Test
    fun `test canExport returns true given state is started and has data for export`() {
        assertTrue(
            State(
                ValidTestSettings,
                Debugger(ComponentDebugStates().toMap().toPersistentMap()),
                StartedTestServerStub
            ).canExport
        )

        assertTrue(
            State(
                ValidTestSettings,
                Debugger(ComponentDebugStates().toMap().toPersistentMap()),
                null
            ).canExport
        )
    }

    @Test
    fun `test isStarted returns true given state is started`() {
        assertTrue(State(ValidTestSettings, server = StartedTestServerStub).isStarted)
    }

    @Test
    fun `test canStart returns true given state is stopped and contain valid settings`() {
        assertTrue(State(ValidTestSettings).canStart)
    }
}
