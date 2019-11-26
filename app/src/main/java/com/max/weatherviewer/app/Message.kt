package com.max.weatherviewer.app

sealed class Message

sealed class Navigation : Message()

data class NavigateTo(val screen: Screen) : Navigation()

object Pop : Navigation()

abstract class ScreenMessage : Message()
