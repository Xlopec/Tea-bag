package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.xlopec.tea.time.travel.plugin.data.InvalidTestSettings
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.State
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
                state = State(settings),
                handler = {}
            )
        }

        onNodeWithTag(HostFieldTag).assertTextEquals(settings.host.input).assertIsEnabled()
        onNodeWithTag(PortFieldTag).assertTextEquals(settings.port.input).assertIsEnabled()
    }

    @Test
    fun `test input fields displayed correctly given an invalid stopped state`() = rule {
        val settings = InvalidTestSettings
        setTestContent {
            SettingsFields(
                state = State(settings),
                handler = {}
            )
        }

        onNodeWithTag(HostFieldTag).assertTextEquals(settings.host.input).assertIsEnabled()
        onNodeWithTag(PortFieldTag).assertTextEquals(settings.port.input).assertIsEnabled()
    }

    @Test
    fun `test input fields displayed correctly given an empty started state`() = rule {
        val settings = ValidTestSettings
        setTestContent {
            SettingsFields(
                state = State(debugger = Debugger(settings = settings), server = StartedTestServerStub),
                handler = {}
            )
        }

        onNodeWithTag(HostFieldTag).assertTextEquals(settings.host.input).assertIsNotEnabled()
        onNodeWithTag(PortFieldTag).assertTextEquals(settings.port.input).assertIsNotEnabled()
    }
}
