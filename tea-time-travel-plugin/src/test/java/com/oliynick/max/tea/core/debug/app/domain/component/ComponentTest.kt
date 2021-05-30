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

package com.oliynick.max.tea.core.debug.app.domain.component

import com.oliynick.max.tea.core.debug.app.component.resolver.AppResolver
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessageChannel
import com.oliynick.max.tea.core.debug.app.component.resolver.HasMessagesChannel
import com.oliynick.max.tea.core.debug.app.component.updater.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest

interface TestEnvironment :
    Updater<TestEnvironment>,
    NotificationUpdater,
    UiUpdater,
    AppResolver<TestEnvironment>,
    HasMessageChannel

@Suppress("FunctionName")
fun TestEnvironment(
    resolver: AppResolver<TestEnvironment>
): TestEnvironment =
    object : TestEnvironment,
        Updater<TestEnvironment> by LiveUpdater(),
        NotificationUpdater by LiveNotificationUpdater,
        UiUpdater by LiveUiUpdater,
        AppResolver<TestEnvironment> by resolver,
        HasMessageChannel by HasMessagesChannel() {}
