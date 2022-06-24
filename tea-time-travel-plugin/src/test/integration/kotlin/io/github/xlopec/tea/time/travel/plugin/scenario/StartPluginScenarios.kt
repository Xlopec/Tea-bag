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
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.feature.notification.ComponentAttached
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.State
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
    fun `test info view displayed when user starts plugin given no components attach`() = rule {
        val environment = TestEnvironment()
        setContentWithEnv(environment) {
            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(environment, Initializer(State(ValidTestSettings))).toStatesComponent(),
            )
        }
        // fixme assertExists should be replaced with assertIsDisplayed
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
    fun `test components displayed when user starts plugin given a component attaches`() = rule {
        val environment = TestEnvironment()
        val messages = MutableSharedFlow<Message>()
        val component = PluginComponent(environment, Initializer(State(ValidTestSettings)))

        setContentWithEnv(environment) {
            Plugin(
                platform = TestPlatform(),
                component = component.toStatesComponent(),
                messages = messages
            )
        }

        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertDoesNotExist()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertDoesNotExist()
        onNode(hasTestTag(ServerActionButtonTag)).performClick()
        // fixme assertExists should be replaced with assertIsDisplayed
        onNode(hasTestTag(InfoViewTag)).assertExists()

        messages.emit(ComponentAttached(TestComponentId, TestSnapshotMeta1, TestUserValue, CollectionWrapper()))

        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertExists()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertExists()
    }
}
