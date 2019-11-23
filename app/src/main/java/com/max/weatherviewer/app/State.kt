package com.max.weatherviewer.app

import com.max.weatherviewer.home.HomeState
import com.max.weatherviewer.home.Loading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

sealed class Screen

data class State(
    val screens: ImmutableList<Screen>
) {

    constructor(screen: Screen) : this(immutableListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

data class Home(val state: HomeState) : Screen() {
    companion object {
        fun initial() = Home(Loading)
    }
}

inline val State.screen: Screen
    get() = screens.last()