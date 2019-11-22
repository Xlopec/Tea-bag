package com.max.weatherviewer

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

sealed class Screen

data class State(val screens: ImmutableList<Screen> = immutableListOf(Home(Loading))) {
    init {
        require(screens.isNotEmpty())
    }
}

data class Home(val state: HomeState) : Screen()

inline val State.screen: Screen
    get() = screens.last()