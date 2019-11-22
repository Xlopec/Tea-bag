package com.max.weatherviewer

sealed class Command

object CloseApp : Command()

sealed class HomeCommand : Command()

data class DoLoadArticles(val query: String) : HomeCommand()