package com.max.weatherviewer.app

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.immutableListOf

abstract class Screen

data class State(
    val screens: ImmutableList<Screen>
) {

    constructor(screen: Screen) : this(immutableListOf(screen))

    init {
        require(screens.isNotEmpty())
    }
}

inline val State.screen: Screen
    get() = screens.last()