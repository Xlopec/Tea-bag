package com.oliynick.max.reader.app

fun PlatformEnv(
    closeCommandsFlow: CloseCommandsSink
): PlatformEnv = object : PlatformEnv {
    override val closeCommands: CloseCommandsSink = closeCommandsFlow
}

actual interface PlatformEnv {
    actual val closeCommands: CloseCommandsSink
}