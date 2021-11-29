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

interface NestedScreen : ScreenState {
    val tabId: ScreenId
}

/**
 * Root screen in the navigation hierarchy
 */
@ImmutableType
interface TabScreen : ScreenState {
    // things for consideration:
    // 1 how to make fast search & update for nested screens
    // 2 how to keep class layout as simple as possible with p1 in mind
    // 3 how to avoid code duplication? (consider Arrow Meta optics API)
    // fixme this probably should go as extensions
    // probably I can make typealias for this and implement all the necessary operations on the top of it
    /* val screens: NavigationStack
     fun pop(): TabScreen
     fun <T : ScreenState> update(
         id: ScreenId,
         how: (T) -> UpdateWith<T, Command>
     ): UpdateWith<TabScreen, Command>*/
}