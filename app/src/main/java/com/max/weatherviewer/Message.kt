package com.max.weatherviewer

sealed class Message

sealed class Navigation : Message()

data class NavigateTo(val screen: Screen) : Navigation()

object Pop : Navigation()