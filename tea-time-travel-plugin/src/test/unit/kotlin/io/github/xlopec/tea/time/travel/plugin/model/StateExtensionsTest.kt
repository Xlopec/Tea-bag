package io.github.xlopec.tea.time.travel.plugin.model

import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugStates
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.DebugState
import kotlin.test.assertTrue
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Test

class StateExtensionsTest {

    @Test
    fun `test canImport returns true given state is started`() {
        assertTrue(Started(ValidTestSettings, DebugState(), StartedTestServerStub).canImport())
    }

    @Test
    fun `test canExport returns true given state is started and has data for export`() {
        assertTrue(
            Started(
                ValidTestSettings,
                DebugState(ComponentDebugStates().toMap().toPersistentMap()),
                StartedTestServerStub
            ).canExport()
        )
    }

    @Test
    fun `test isStarted returns true given state is started`() {
        assertTrue(Started(ValidTestSettings, DebugState(), StartedTestServerStub).isStarted())
    }

    @Test
    fun `test canStart returns true given state is stopped and contain valid settings`() {
        assertTrue(Stopped(ValidTestSettings).canStart())
    }
}
