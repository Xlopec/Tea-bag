@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.component

import java.net.URL

fun URL(
    protocol: String = "http",
    host: String = "localhost",
    port: UInt = 8080U
): URL = URL(protocol, host, port.toInt(), "")