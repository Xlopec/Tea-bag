package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.feature.storage.LocalStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

expect val IO: CoroutineDispatcher

expect interface Environment : AppModule<Environment>, CoroutineScope, LocalStorage