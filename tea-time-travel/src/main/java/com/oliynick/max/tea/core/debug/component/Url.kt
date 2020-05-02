@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.component

import java.net.URL

/**
 * Url builder
 *
 * This is just a shortcut for
 * ```kotlin
 * URL(protocol, host, port.toInt(), "")
 * ```
 */
fun URL(
    protocol: String = "http",
    host: String = "localhost",
    port: UInt = 8080U
): URL = URL(protocol, host, port.toInt(), "")