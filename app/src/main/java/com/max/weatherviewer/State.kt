package com.max.weatherviewer

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

sealed class Screen

data class State(val screens: ImmutableList<Screen> = immutableListOf(Home())) {
    init {
        require(screens.isNotEmpty())
    }
}

class Home : Screen()