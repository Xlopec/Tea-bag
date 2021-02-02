package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import com.intellij.openapi.Disposable

object NoOpDisposable : Disposable {
    override fun dispose() = Unit
}
