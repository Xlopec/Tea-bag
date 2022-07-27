package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId1
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
     * * Given a valid stopped state
     * * When user clicks on `start` button
     * * Then check info view displayed
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
        onNodeWithTag(InfoViewTag).assertExists()

        onNodeWithTag(ServerActionButtonTag).performClick()

        awaitIdle()
        onNodeWithTag(InfoViewTag).assertExists()
    }

    /**
     * Scenario:
     * * Given a valid stopped state
     * * When user clicks on `start` button
     * * Then check info view displayed
     * * When new component attaches to plugin
     * * Then tab and component content are rendered
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
        onNodeWithTag(ComponentTabTag(TestComponentId1)).assertDoesNotExist()
        onNodeWithTag(ComponentTag(TestComponentId1)).assertDoesNotExist()
        onNodeWithTag(ServerActionButtonTag).performClick()
        // fixme assertExists should be replaced with assertIsDisplayed
        onNodeWithTag(InfoViewTag).assertExists()

        messages.emit(ComponentAttached(TestComponentId1, TestSnapshotMeta1, TestUserValue, CollectionWrapper()))

        awaitIdle()
        onNodeWithTag(ComponentTabTag(TestComponentId1)).assertExists()
        onNodeWithTag(ComponentTag(TestComponentId1)).assertExists()
    }
}
