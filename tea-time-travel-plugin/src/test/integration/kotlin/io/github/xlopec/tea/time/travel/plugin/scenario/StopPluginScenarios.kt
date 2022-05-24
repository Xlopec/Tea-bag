package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotMeta1
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestProject
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.ComponentState
import io.github.xlopec.tea.time.travel.plugin.feature.component.model.DebugState
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Started
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class StopPluginScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Running plugin with no components attached
     * * Check no components rendered
     * * User clicks on `stop` button
     * * Check info view displayed
     */
    @Test
    fun `test info view displayed when user stops plugin server given empty started state`() = rule {
        val started = Started(ValidTestSettings, DebugState(), StartedTestServerStub)
        val environment = TestEnvironment()

        setContentWithEnv(environment) {

            Plugin(
                project = TestProject(),
                component = PluginComponent(environment, Initializer(started)).toStatesComponent(),
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
     * * Running plugin with some components attached
     * * Check components rendered
     * * User clicks on `stop` button
     * * Check info view displayed
     * * Check component content is cleared
     */
    @Test
    fun `test info view displayed when user stops plugin server given non-empty started state`() = rule {
        val debugState = DebugState(
            persistentMapOf(
                TestComponentId to ComponentState(
                    TestComponentId,
                    TestUserValue,
                    persistentListOf(OriginalSnapshot(TestSnapshotMeta1, null, TestUserValue, CollectionWrapper(TestUserValue)))
                )
            )
        )
        val messages = MutableSharedFlow<Message>()
        val started = Started(ValidTestSettings, debugState, StartedTestServerStub)
        val environment = TestEnvironment()

        val component = PluginComponent(environment, Initializer(started))

        setContentWithEnv(environment) {
            Plugin(
                project = TestProject(),
                component = component.toStatesComponent(),
                messages = messages
            )
        }

        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertExists()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertExists()

        onNode(hasTestTag(ServerActionButtonTag)).performClick()
        // fixme should assertExists should be replaced with assertIsDisplayed
        awaitIdle()
        onNode(hasTestTag(InfoViewTag)).assertExists()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertDoesNotExist()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertDoesNotExist()
    }
}
