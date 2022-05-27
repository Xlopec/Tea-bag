package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugStates
import io.github.xlopec.tea.time.travel.plugin.data.InvalidTestSettings
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setTestContent
import kotlinx.collections.immutable.toPersistentMap
import org.junit.Rule
import org.junit.Test

class BottomMenuTests {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `test action buttons displayed correctly given a valid stopped state`() = rule {
        setTestContent {
            BottomActionMenu(
                onImportSession = {},
                onExportSession = {},
                state = State(ValidTestSettings),
                events = {}
            )
        }

        onNode(hasTestTag(ImportButtonTag)).assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertIsEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given an invalid stopped state`() = rule {
        setTestContent {
            BottomActionMenu(
                onImportSession = {},
                onExportSession = {},
                state = State(InvalidTestSettings),
                events = {}
            )
        }

        onNode(hasTestTag(ImportButtonTag)).assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertIsNotEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given an empty started state`() = rule {
        setTestContent {
            BottomActionMenu(
                onImportSession = {},
                onExportSession = {},
                state = State(ValidTestSettings, server = StartedTestServerStub),
                events = {}
            )
        }

        onNode(hasTestTag(ImportButtonTag)).assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertIsEnabled()
    }

    @Test
    fun `test action buttons displayed correctly given a non-empty started state`() = rule {
        setTestContent {
            val started = State(ValidTestSettings, Debugger(ComponentDebugStates().toMap().toPersistentMap()), StartedTestServerStub)

            BottomActionMenu(
                onImportSession = {},
                onExportSession = {},
                state = started,
                events = {}
            )
        }

        onNode(hasTestTag(ImportButtonTag)).assertIsEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertIsEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertIsEnabled()
    }
}
