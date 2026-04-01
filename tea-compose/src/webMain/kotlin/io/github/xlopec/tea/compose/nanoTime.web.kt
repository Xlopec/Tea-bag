package io.github.xlopec.tea.compose

import kotlinx.browser.window

internal actual fun nanoTime(): Long = window.performance.now().toLong() * 1_000_000