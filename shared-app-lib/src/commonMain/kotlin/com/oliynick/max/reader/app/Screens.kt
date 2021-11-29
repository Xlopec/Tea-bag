package com.oliynick.max.reader.app

@ImmutableType
sealed interface ScreenState {
    val id: ScreenId
}

/**
 * Intermediate screen that should be rendered without
 * tab frame
 */
interface FullScreen : ScreenState

/**
 * Screen that should be drawn inside tab
 */
interface NestedScreen : ScreenState {
    val tabId: ScreenId
}

/**
 * Root screen in the navigation hierarchy
 */
interface TabScreen : ScreenState