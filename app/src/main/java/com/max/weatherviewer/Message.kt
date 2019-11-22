package com.max.weatherviewer

sealed class Message

sealed class Navigation : Message()

data class NavigateTo(val screen: Screen) : Navigation()

object Pop : Navigation()

sealed class HomeMessage : Message()

data class LoadArticles(val query: String) : HomeMessage()

data class ArticlesLoaded(val articles: List<Article>) : HomeMessage()
