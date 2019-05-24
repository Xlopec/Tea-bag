package com.max.weatherviewer

sealed class UserAction {
    object LoadButtonClicked : UserAction()
    object Refresh : UserAction()
}