package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugStates
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Test
import kotlin.test.assertTrue

class StateExtensionsTest {

    @Test
    fun `test canExport returns true given state is started and has data for export`() {
        assertTrue(
            State(
                debugger = Debugger(
                    settings = ValidTestSettings,
                    components = ComponentDebugStates().toMap().toPersistentMap()
                ),
                server = StartedTestServerStub
            ).canExport
        )

        assertTrue(
            State(
                debugger = Debugger(
                    settings = ValidTestSettings,
                    components = ComponentDebugStates().toMap().toPersistentMap()
                ),
                server = null
            ).canExport
        )
    }

    @Test
    fun `test isStarted returns true given state is started`() {
        assertTrue(State(debugger = Debugger(ValidTestSettings), server = StartedTestServerStub).isStarted)
    }

    @Test
    fun `test canStart returns true given state is stopped and contain valid settings`() {
        assertTrue(State(settings = ValidTestSettings).canStart)
    }
}
