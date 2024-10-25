@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.ComponentDebugState
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.TestAppStateValue
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId1
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotId1
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotId2
import io.github.xlopec.tea.time.travel.plugin.data.TestTimestamp1
import io.github.xlopec.tea.time.travel.plugin.data.TestTimestamp2
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Tag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.feature.notification.AppendSnapshot
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotMeta
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.onCloseTabNode
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        onNodeWithText(idA.value).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithText(idB.value).assertExists(TabDoesNotExistMessage(idB)).assertIsNotSelected()
        onNodeWithText(idC.value).assertExists(TabDoesNotExistMessage(idC)).assertIsNotSelected()

        onCloseTabNode(idB).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithText(idA.value).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithText(idB.value).assertDoesNotExist()
        onNodeWithText(idC.value).assertExists(TabDoesNotExistMessage(idC)).assertIsNotSelected()

        onCloseTabNode(idC).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithText(idA.value).assertExists(TabDoesNotExistMessage(idA)).assertIsSelected()
        onNodeWithText(idB.value).assertDoesNotExist()
        onNodeWithText(idC.value).assertDoesNotExist()

        onCloseTabNode(idA).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithText(idA.value).assertDoesNotExist()
        onNodeWithText(idB.value).assertDoesNotExist()
        onNodeWithText(idC.value).assertDoesNotExist()
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
        onNodeWithText(idB.value).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithText(idB.value).assertIsSelected()

        onNodeWithText(idC.value).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()

        onNodeWithText(idC.value).assertIsSelected()
    }

    /**
     * Scenario:
     * * Given a running plugin
     * * When component attaches with id [TestComponentId1] and initial snapshot with id [TestSnapshotId1]
     * * When component emits snapshots with id [TestSnapshotId2]
     * * Then check that snapshots with ids [TestSnapshotId1] and [TestSnapshotId2] exist in composition
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test component snapshots are rendered when new snapshots are emitted`() = rule {
        val scheduler = TestCoroutineScheduler()
        val environment = TestEnvironment(scope = CoroutineScope(UnconfinedTestDispatcher(scheduler)))
        val messages = MutableSharedFlow<Message>()

        val snapshotMeta1 = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val snapshotMeta2 = SnapshotMeta(TestSnapshotId2, TestTimestamp2)

        setContentWithEnv(environment) {
            val debugger = Debugger(
                settings = ValidTestSettings,
                components = persistentMapOf(
                    ComponentDebugState(
                        componentId = TestComponentId1,
                        snapshots = persistentListOf(
                            OriginalSnapshot(
                                meta = snapshotMeta1,
                                message = StringWrapper("message"),
                                state = TestUserValue,
                                commands = CollectionWrapper()
                            )
                        ),
                        filteredSnapshots = persistentListOf(
                            FilteredSnapshot(
                                meta = snapshotMeta1,
                                message = StringWrapper("message"),
                                state = TestUserValue,
                                commands = CollectionWrapper()
                            )
                        ),
                        state = TestUserValue
                    )
                )
            )

            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(
                    environment = environment,
                    initializer = Initializer(State(debugger, StartedTestServerStub))
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
                componentId = TestComponentId1,
                meta = snapshotMeta2,
                message = StringWrapper("nullify"),
                oldState = TestUserValue,
                newState = TestAppStateValue,
                commands = CollectionWrapper()
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
