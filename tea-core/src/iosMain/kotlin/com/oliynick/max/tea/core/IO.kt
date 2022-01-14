package com.oliynick.max.tea.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * On ios IO is backed by [Dispatchers.Default]
 */
public actual val IO: CoroutineDispatcher = Dispatchers.Default