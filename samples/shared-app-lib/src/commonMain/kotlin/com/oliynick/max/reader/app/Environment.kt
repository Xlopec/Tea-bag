package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope

expect interface Environment : AppModule<Environment>, CoroutineScope, LocalStorage