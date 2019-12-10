package com.max.weatherviewer

import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.home.LoadCriteria

sealed class Command

// App wide commands

object CloseApp : Command()

// Feed screen commands

data class LoadByCriteria(
    val id: ScreenId,
    val criteria: LoadCriteria
) : Command()
