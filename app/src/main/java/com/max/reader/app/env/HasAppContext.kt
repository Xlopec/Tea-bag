@file:Suppress("FunctionName")

package com.max.reader.app.env

import android.app.Application

interface HasAppContext {
    val application: Application
}

fun AppContext(
    application: Application
) = object : HasAppContext {
    override val application: Application = application
}
