package com.oliynick.max.tea.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * On jvm IO is backed by [Dispatchers.IO]
 */
public actual val IO: CoroutineDispatcher = Dispatchers.IO