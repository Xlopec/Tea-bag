package com.oliynick.max.tea.core.debug.app.misc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

inline val CoroutineScope.job: Job?
    get() = coroutineContext[Job.Key]

