/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.component.cms

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.oliynick.max.tea.core.component.effect
import com.oliynick.max.tea.core.component.sideEffect
import com.oliynick.max.tea.core.debug.app.component.cms.command.*
import com.oliynick.max.tea.core.debug.app.component.cms.message.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.misc.settings
import com.oliynick.max.tea.core.debug.app.presentation.ui.balloon.showBalloon
import com.oliynick.max.tea.core.debug.app.transport.Server
import com.oliynick.max.tea.core.debug.app.transport.ServerImpl
import com.oliynick.max.tea.core.debug.app.transport.serialization.toJsonElement
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ApplyState
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext

fun LiveAppResolver(
    project: Project,
    properties: PropertiesComponent,
    events: MutableSharedFlow<Message>,
): AppResolver = LiveAppResolverImpl(project, properties, events)

private class LiveAppResolverImpl(
    private val project: Project,
    private val properties: PropertiesComponent,
    private val events: MutableSharedFlow<Message>,
) : AppResolver {

    override suspend fun resolve(
        command: Command,
    ): Set<Message> =
        runCatching { doResolve(command) }
            .getOrElse { th -> setOf(NotifyOperationException(th, command)) }

    suspend fun doResolve(
        command: Command,
    ): Set<NotificationMessage> =
        when (command) {
            is DoStoreSettings -> command sideEffect { properties.settings = settings }
            // fixme remove later
            is DoStartServer -> command effect { NotifyStarted(startServer(address)) }
            /*is DoStartServer -> setOf(with(command) { NotifyStarted(newServer(address, events)); },
                ComponentAttached(
                    componentId,
                    appState
                ),
                AppendSnapshot(
                    componentId,
                    SnapshotMeta(
                        SnapshotId(UUID.randomUUID()),
                        LocalDateTime.now()
                    ),
                    appState,
                    appState,
                    appState
                ),
                AppendSnapshot(
                    componentId,
                    SnapshotMeta(
                        SnapshotId(UUID.randomUUID()),
                        LocalDateTime.now()
                    ),
                    appState,
                    appState,
                    appState
                ),
                AppendSnapshot(
                    componentId,
                    SnapshotMeta(
                        SnapshotId(UUID.randomUUID()),
                        LocalDateTime.now()
                    ),
                    appState,
                    appState,
                    appState
                )
            // fixme end of removal section
            )*/
            is DoStopServer -> command effect { server.stop(); NotifyStopped }
            is DoApplyMessage -> command sideEffect {
                server(id, ApplyMessage(command.command.toJsonElement()))
            }
            is DoApplyState -> reApplyState(command)
            is DoNotifyOperationException -> command sideEffect {
                project.showBalloon(ExceptionBalloon(exception, operation))
            }
            is DoWarnUnacceptableMessage -> command sideEffect {
                project.showBalloon(UnacceptableMessageBalloon(message, state))
            }
            is DoNotifyComponentAttached -> command sideEffect {
                project.showBalloon(ComponentAttachedBalloon(componentId))
            }
        }

    private suspend fun reApplyState(
        command: DoApplyState,
    ) = command effect {
        server(id, ApplyState(state.toJsonElement()))
        project.showBalloon(StateAppliedBalloon(id))
        StateApplied(id, state)
    }

    private suspend fun startServer(
        address: ServerAddress
    ): Server = withContext(Dispatchers.IO) {
        val newServer = ServerImpl.newInstance(address, events)
        newServer.start()
        newServer
    }

}


val user = Ref(
    Type.of("com.max.oliynick.Test"),
    setOf(
        Property("name", StringWrapper("Max")),
        Property("surname", StringWrapper("Oliynick")),
        Property(
            "contacts", Ref(
                Type.of("com.max.oliynick.Contact"),
                setOf(
                    Property(
                        "site", Ref(
                            Type.of("java.util.URL"),
                            setOf(
                                Property("domain", StringWrapper("google")),
                                Property("port", NumberWrapper(8080)),
                                Property("protocol", StringWrapper("https"))
                            ),
                        )
                    ),
                )
            )
        ),
        Property("position", StringWrapper("Developer")),
    )
)

val componentId = ComponentId("Test component id")

val appState =
    Ref(
        Type.of("app.State"),
        setOf(
            Property(
                "users",
                CollectionWrapper(
                    listOf(
                        user,
                        user,
                        user,
                        user,
                        user,
                    )
                )
            )
        )
    )
