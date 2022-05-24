package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotMeta1
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestProject
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentAttached
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Stopped
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class StartPluginScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Valid stopped state
     * * User clicks on `start` button
     * * Check info view displayed
     */
    @Test
    fun `sp1 - test info view displayed for empty started state`() = rule {
        val environment = TestEnvironment()
        setContentWithEnv(environment) {

            Plugin(
                project = TestProject(),
                component = PluginComponent(environment, Initializer(Stopped(ValidTestSettings))).toStatesComponent(),
            )
        }
        // fixme should assertExists should be replaced with assertIsDisplayed
        onNode(hasTestTag(InfoViewTag)).assertExists()

        onNode(hasTestTag(ServerActionButtonTag)).performClick()

        awaitIdle()
        onNode(hasTestTag(InfoViewTag)).assertExists()
    }

    /**
     * Scenario:
     * * Valid stopped state
     * * User clicks on `start` button
     * * Check info view displayed
     * * New component attaches to plugin
     * * Tab and component content are rendered
     */
    @Test
    fun `sp2 - test components displayed for non-empty started state`() = rule {
        val environment = TestEnvironment()
        val messages = MutableSharedFlow<Message>()
        val component = PluginComponent(environment, Initializer(Stopped(ValidTestSettings)))

        setContentWithEnv(environment) {
            Plugin(
                project = TestProject(),
                component = component.toStatesComponent(),
                messages = messages
            )
        }

        awaitIdle()
        onNode(hasTestTag(ServerActionButtonTag)).performClick()
        // fixme should assertExists should be replaced with assertIsDisplayed
        onNode(hasTestTag(InfoViewTag)).assertExists()

        messages.emit(ComponentAttached(TestComponentId, TestSnapshotMeta1, TestUserValue, CollectionWrapper()))

        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertExists()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertExists()
    }
}
