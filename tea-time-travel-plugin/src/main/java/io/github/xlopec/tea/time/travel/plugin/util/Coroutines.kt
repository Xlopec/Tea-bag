package io.github.xlopec.tea.time.travel.plugin.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.CoroutineSupport
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

@Suppress("UnstableApiUsage", "UnusedReceiverParameter")
val Dispatchers.Edt: CoroutineContext
    get() = ApplicationManager.getApplication().getService(CoroutineSupport::class.java).edtDispatcher()
