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

package com.oliynick.max.tea.core.debug.app.misc

import com.oliynick.max.tea.core.debug.app.domain.*
import io.kotlintest.properties.Gen

val TestHost = Host.of("localhost")!!
val TestPort = Port.of(123)

object HostGen : Gen<Validated<Host>> {

    override fun constants() = listOf(
            Invalid("", ""),
            Invalid("some", "message")
    )

    override fun random() = sequenceOf(
            Invalid("", ""),
            Valid(
                    TestHost.value,
                    TestHost
            ),
            Invalid("some", "message"),
            Valid(
                    "www.google.com",
                    Host.of("www.google.com")!!
            )
    )

}

object PortGen : Gen<Validated<Port>> {

    override fun constants() = listOf(
            Invalid("", ""),
            Invalid("some", "message")
    )

    override fun random() = sequenceOf(
            Invalid("", ""),
            Valid(
                    TestPort.value.toString(),
                    TestPort
            ),
            Invalid("some", "message"),
            Valid(
                    "100",
                    Port.of("100")!!
            )
    )

}

val SettingsGen = Gen.bind(HostGen, PortGen) { h, p -> Settings(h, p, false) }
