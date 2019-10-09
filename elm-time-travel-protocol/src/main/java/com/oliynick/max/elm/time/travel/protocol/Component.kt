package com.oliynick.max.elm.time.travel.protocol

data class ComponentId(val id: String) {
    init {
        require(id.isNotBlank() && id.isNotEmpty())
    }
}