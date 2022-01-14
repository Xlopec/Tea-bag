package com.oliynick.max.tea.core.debug.component

class ComponentException(message: String? = null, cause: Throwable? = null) :
    IllegalStateException(message, cause)