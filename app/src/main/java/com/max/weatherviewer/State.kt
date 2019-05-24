package com.max.weatherviewer

import com.max.weatherviewer.model.Weather

sealed class State {

    object Loading : State()

    data class Preview(val data: Weather? = null) : State() {
        companion object {
            private val EMPTY = Preview()
            fun empty() = EMPTY
        }
    }

    data class Failure(val th: Throwable) : State()

}