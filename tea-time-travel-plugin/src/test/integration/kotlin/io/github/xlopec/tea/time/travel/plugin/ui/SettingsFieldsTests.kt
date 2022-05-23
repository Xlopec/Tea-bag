package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.xlopec.tea.time.travel.plugin.data.InvalidTestSettings
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.DebugState
import io.github.xlopec.tea.time.travel.plugin.model.Started
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setTestContent
import org.junit.Rule
import org.junit.Test

class SettingsFieldsTests {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `test input fields displayed correctly given a valid stopped state`() = rule {
        val settings = ValidTestSettings
        setTestContent {
            SettingsFields(
                state = Stopped(settings),
                events = {}
            )
        }

        onNode(hasTestTag(HostFieldTag)).assertTextEquals(settings.host.input).assertIsEnabled()
        onNode(hasTestTag(PortFieldTag)).assertTextEquals(settings.port.input).assertIsEnabled()
    }

    @Test
    fun `test input fields displayed correctly given an invalid stopped state`() = rule {
        val settings = InvalidTestSettings
        setTestContent {
            SettingsFields(
                state = Stopped(settings),
                events = {}
            )
        }

        onNode(hasTestTag(HostFieldTag)).assertTextEquals(settings.host.input).assertIsEnabled()
        onNode(hasTestTag(PortFieldTag)).assertTextEquals(settings.port.input).assertIsEnabled()
    }

    @Test
    fun `test input fields displayed correctly given an empty started state`() = rule {
        val settings = ValidTestSettings
        setTestContent {
            SettingsFields(
                state = Started(ValidTestSettings, DebugState(), StartedTestServerStub),
                events = {}
            )
        }

        onNode(hasTestTag(HostFieldTag)).assertTextEquals(settings.host.input).assertIsNotEnabled()
        onNode(hasTestTag(PortFieldTag)).assertTextEquals(settings.port.input).assertIsNotEnabled()
    }
}
