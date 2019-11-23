package com.max.weatherviewer

sealed class Command

// App wide commands

object CloseApp : Command()

// Home screen commands

sealed class HomeCommand : Command()

data class DoLoadArticles(val query: String) : HomeCommand()

