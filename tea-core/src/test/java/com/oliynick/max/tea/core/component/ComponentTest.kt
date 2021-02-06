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

@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.Env
import core.component.BasicComponentTest
import kotlinx.coroutines.CoroutineScope
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComponentTest : BasicComponentTest(::ComponentFactory) {

    private companion object {

        fun ComponentFactory(
            @Suppress("UNUSED_PARAMETER") scope: CoroutineScope,
            env: Env<Char, String, Char>,
        ): Component<Char, String, Char> = Component(env)
    }

}
