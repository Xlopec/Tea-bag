package io.github.xlopec.tea.time.travel.plugin.integration

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugStates
import io.github.xlopec.tea.time.travel.plugin.data.InvalidTestSettings
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.DebugState
import io.github.xlopec.tea.time.travel.plugin.integration.environment.TestProject
import io.github.xlopec.tea.time.travel.plugin.integration.util.invoke
import io.github.xlopec.tea.time.travel.plugin.integration.util.setTestContent
import io.github.xlopec.tea.time.travel.plugin.model.Started
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.ui.ExportButtonTag
import io.github.xlopec.tea.time.travel.plugin.ui.ImportButtonTag
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Rule
import org.junit.Test

class BottomMenuTests {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `test action buttons displayed correctly given a valid stopped state`() = rule {
        setTestContent {
            Plugin(TestProject(), Stopped(ValidTestSettings)) {}
        }

        onNode(hasTestTag(ImportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertExists().assertIsEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given an invalid stopped state`() = rule {
        setTestContent {
            Plugin(TestProject(), Stopped(InvalidTestSettings)) {}
        }

        onNode(hasTestTag(ImportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertExists().assertIsNotEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given an empty started state`() = rule {
        setTestContent {
            Plugin(TestProject(), Started(ValidTestSettings, DebugState(), StartedTestServerStub)) {}
        }

        onNode(hasTestTag(ImportButtonTag)).assertExists().assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertExists().assertIsEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given a non-empty started state`() = rule {
        setTestContent {
            val started = Started(ValidTestSettings, DebugState(ComponentDebugStates().toMap().toPersistentMap()), StartedTestServerStub)
            Plugin(TestProject(), started) {}
        }

        onNode(hasTestTag(ImportButtonTag)).assertExists().assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertExists().assertIsEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertExists().assertIsEnabled()
    }
}
