@file:Suppress("unused")

package com.oliynick.max.elm.time.travel.app.exception

import com.google.gson.annotations.SerializedName

internal class ErrorResponse private constructor(@SerializedName("message") private val message: String) {

    constructor(th: Throwable) : this(th.localizedMessage ?: "Unknown error")

}