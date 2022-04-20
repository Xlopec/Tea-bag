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

package io.github.xlopec.tea.time.travel.plugin.state

import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.plugin.domain.ServerAddress
import io.github.xlopec.tea.time.travel.protocol.ComponentId

interface Server {

    val address: ServerAddress

    suspend operator fun invoke(
        component: ComponentId,
        message: GsonClientMessage
    )

    suspend fun stop()
}
