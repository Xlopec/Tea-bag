package com.oliynick.max.tea.core.debug.app.misc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

fun CoroutineScope.childScope() = CoroutineScope(coroutineContext + Job(coroutineContext[Job.Key]))
