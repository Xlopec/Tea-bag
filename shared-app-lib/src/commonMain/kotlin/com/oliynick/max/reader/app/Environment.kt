package com.oliynick.max.reader.app

import kotlinx.coroutines.CoroutineScope

expect interface Environment : AppModule<Environment>, CoroutineScope
