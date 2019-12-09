package com.max.weatherviewer

import com.max.weatherviewer.home.LoadCriteria

sealed class Command

// App wide commands

object CloseApp : Command()

// Feed screen commands

data class LoadByCriteria(
    val criteria: LoadCriteria
) : Command()
