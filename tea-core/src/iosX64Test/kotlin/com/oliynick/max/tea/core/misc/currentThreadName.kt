package com.oliynick.max.tea.core.misc

import platform.Foundation.NSThread

internal actual fun currentThreadName(): String = NSThread.currentThread.toString()