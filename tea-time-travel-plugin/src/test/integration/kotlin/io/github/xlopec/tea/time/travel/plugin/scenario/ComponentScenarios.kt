@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.*
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Tag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.feature.notification.AppendSnapshot
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.onCloseTabActionNode
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.Rule
import org.junit.Test

class ComponentScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Given a running plugin with multiple components attached - 'Component A', 'Component B' and 'Component C'
     * * When user closes 'Component B'
     * * Then check 'Component A' & 'Component C' remain
     * * When user closes 'Component C'
     * * Then check 'Component A' remains
     * * When user closes 'Component A'
     * * Then check info view is displayed
     */
    @Test
    fun `test component tabs are closed properly`() = rule {
        val scheduler = TestCoroutineScheduler()
        val environment = TestEnvironment(scope = CoroutineScope(StandardTestDispatcher(scheduler)))
        val messages = MutableSharedFlow<Message>()
        val idA = ComponentId("Component A")
        val idB = ComponentId("Component B")
        val idC = ComponentId("Component C")

        setContentWithEnv(environment) {
            val debugger = Debugger(
                ValidTestSettings,
                persistentMapOf(
                    ComponentDebugState(idA),
                    ComponentDebugState(idB),
                    ComponentDebugState(idC),
                )
            )

            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(
                    environment,
                    Initializer(State(debugger, StartedTestServerStub))
                ).toStatesComponent(),
                messages = messages
            )
        }

        scheduler.advanceUntilIdle()
        awaitIdle()
        onNodeWithTag(ComponentTabTag(idA)).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithTag(ComponentTabTag(idB)).assertExists(TabDoesNotExistMessage(idB)).assertIsNotSelected()
        onNodeWithTag(ComponentTabTag(idC)).assertExists(TabDoesNotExistMessage(idC)).assertIsNotSelected()

        onCloseTabActionNode(idB).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(ComponentTabTag(idA)).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithTag(ComponentTabTag(idB)).assertDoesNotExist()
        onNodeWithTag(ComponentTabTag(idC)).assertExists(TabDoesNotExistMessage(idC)).assertIsNotSelected()

        onCloseTabActionNode(idC).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(ComponentTabTag(idA)).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithTag(ComponentTabTag(idB)).assertDoesNotExist()
        onNodeWithTag(ComponentTabTag(idC)).assertDoesNotExist()

        onCloseTabActionNode(idA).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(ComponentTabTag(idA)).assertDoesNotExist()
        onNodeWithTag(ComponentTabTag(idB)).assertDoesNotExist()
        onNodeWithTag(ComponentTabTag(idC)).assertDoesNotExist()
        onNodeWithTag(InfoViewTag).assertExists()
    }

    /**
     * Scenario:
     * * Given a running plugin with multiple components attached - 'Component A', 'Component B' and 'Component C'
     * * When user selects 'Component B'
     * * Then tab 'Component B' becomes selected
     * * When user selects 'Component C'
     * * Then tab 'Component C' becomes selected
     */
    @Test
    fun `test component tab gets selected when user clicks on it`() = rule {
        val scheduler = TestCoroutineScheduler()
        val environment = TestEnvironment(scope = CoroutineScope(StandardTestDispatcher(scheduler)))
        val messages = MutableSharedFlow<Message>()
        val idA = ComponentId("Component A")
        val idB = ComponentId("Component B")
        val idC = ComponentId("Component C")

        setContentWithEnv(environment) {
            val debugger = Debugger(
                ValidTestSettings,
                persistentMapOf(
                    ComponentDebugState(idA),
                    ComponentDebugState(idB),
                    ComponentDebugState(idC),
                )
            )

            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(
                    environment,
                    Initializer(State(debugger, StartedTestServerStub))
                ).toStatesComponent(),
                messages = messages
            )
        }

        scheduler.advanceUntilIdle()
        awaitIdle()
        onNodeWithTag(ComponentTabTag(idB)).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(ComponentTabTag(idB)).assertIsSelected()

        onNodeWithTag(ComponentTabTag(idC)).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(ComponentTabTag(idC)).assertIsSelected()
    }

    /**
     * Scenario:
     * * Given a running plugin
     * * When component attaches with id [TestComponentId1] and initial snapshot with id [TestSnapshotId1]
     * * When component emits snapshots with id [TestSnapshotId2]
     * * Then check that snapshots with ids [TestSnapshotId1] and [TestSnapshotId2] exist in composition
     */
    @Test
    fun `test component snapshots are rendered when new snapshots are emitted`() = rule {
        val scheduler = TestCoroutineScheduler()
        val environment = TestEnvironment(scope = CoroutineScope(StandardTestDispatcher(scheduler)))
        val messages = MutableSharedFlow<Message>()

        val snapshotMeta1 = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val snapshotMeta2 = SnapshotMeta(TestSnapshotId2, TestTimestamp2)

        setContentWithEnv(environment) {
            val debugger = Debugger(
                ValidTestSettings,
                persistentMapOf(
                    ComponentDebugState(
                        TestComponentId1,
                        persistentListOf(
                            OriginalSnapshot(
                                snapshotMeta1,
                                StringWrapper("message"),
                                TestUserValue,
                                CollectionWrapper()
                            )
                        ),
                        persistentListOf(
                            FilteredSnapshot(
                                snapshotMeta1,
                                StringWrapper("message"),
                                TestUserValue,
                                CollectionWrapper()
                            )
                        ),
                        TestUserValue
                    )
                )
            )

            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(
                    environment,
                    Initializer(State(debugger, StartedTestServerStub))
                ).toStatesComponent(),
                messages = messages
            )
        }

        scheduler.advanceUntilIdle()
        awaitIdle()
        // fixme assertExists should be replaced with assertIsDisplayed
        onNodeWithTag(InfoViewTag).assertDoesNotExist()
        onNodeWithTag(Tag(snapshotMeta1)).assertExists()
        onNodeWithTag(Tag(snapshotMeta2)).assertDoesNotExist()

        messages.emit(
            AppendSnapshot(
                TestComponentId1,
                snapshotMeta2,
                StringWrapper("nullify"),
                TestUserValue,
                TestAppStateValue,
                CollectionWrapper()
            )
        )
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithTag(Tag(snapshotMeta1)).assertExists()
        onNodeWithTag(Tag(snapshotMeta2)).assertExists()
    }

    private fun TabDoesNotExistMessage(
        id: ComponentId
    ) = "tab with id $id doesn't exist"
}
