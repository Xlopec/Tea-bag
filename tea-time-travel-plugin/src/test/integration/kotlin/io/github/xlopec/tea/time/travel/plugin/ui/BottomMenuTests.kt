package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

        onNodeWithTag(ImportButtonTag).assertIsEnabled()
        onNodeWithTag(ExportButtonTag).assertIsNotEnabled()
        onNodeWithTag(ServerActionButtonTag).assertIsEnabled()
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

        onNodeWithTag(ImportButtonTag).assertIsEnabled()
        onNodeWithTag(ExportButtonTag).assertIsNotEnabled()
        onNodeWithTag(ServerActionButtonTag).assertIsNotEnabled()
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

        onNodeWithTag(ImportButtonTag).assertIsEnabled()
        onNodeWithTag(ExportButtonTag).assertIsNotEnabled()
        onNodeWithTag(ServerActionButtonTag).assertIsEnabled()
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

        onNodeWithTag(ImportButtonTag).assertIsEnabled()
        onNodeWithTag(ExportButtonTag).assertIsEnabled()
        onNodeWithTag(ServerActionButtonTag).assertIsEnabled()
    }
}
