package io.github.xlopec.tea.navigation

import platform.Foundation.NSLog

public actual fun debugLog(message: String) {
    NSLog("%s", message)
}
