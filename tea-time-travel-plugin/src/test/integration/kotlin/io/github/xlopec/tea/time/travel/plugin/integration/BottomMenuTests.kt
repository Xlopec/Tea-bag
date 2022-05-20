package io.github.xlopec.tea.time.travel.plugin.integration

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.xlopec.tea.time.travel.plugin.integration.data.ValidSettings
import io.github.xlopec.tea.time.travel.plugin.integration.environment.TestProject
import io.github.xlopec.tea.time.travel.plugin.integration.util.invoke
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.ui.ExportButtonTag
import io.github.xlopec.tea.time.travel.plugin.ui.ImportButtonTag
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import org.junit.Rule
import org.junit.Test

class BottomMenuTests {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `test action buttons are displayed correctly given a valid stopped state`() = rule {
        setContent {
            Plugin(TestProject(), Stopped(ValidSettings)) {}
        }

        onNode(hasTestTag(ImportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ExportButtonTag)).assertExists().assertIsNotEnabled()
        onNode(hasTestTag(ServerActionButtonTag)).assertExists().assertIsEnabled()
    }
}
