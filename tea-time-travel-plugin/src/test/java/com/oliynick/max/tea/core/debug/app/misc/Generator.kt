package com.oliynick.max.tea.core.debug.app.misc

import com.oliynick.max.tea.core.debug.app.domain.*
import io.kotlintest.properties.Gen

val TestHost = Host.of("localhost")!!
val TestPort = Port.of(123U)

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