/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.max.reader.app

import com.max.reader.app.NavigateToFavorite.id
import com.max.reader.app.NavigateToFeed.id
import com.max.reader.app.NavigateToSettings.id
import com.max.reader.app.NavigateToTrending.id
import com.max.reader.domain.Article
import com.max.reader.screens.settings.SettingsState
import java.util.*
import java.util.UUID.randomUUID

sealed class Message

sealed class Navigation : Message()

object NavigateToFeed : Navigation() {
    val id: ScreenId = randomUUID()
}

object NavigateToFavorite : Navigation() {
    val id: ScreenId = randomUUID()
}

object NavigateToTrending : Navigation() {
    val id: ScreenId = randomUUID()
}

object NavigateToSettings : Navigation() {
    val id: ScreenId = SettingsState.id
}

object Pop : Navigation()

data class NavigateToArticleDetails(
    val article: Article,
    val screenId: UUID = randomUUID(),
) : Navigation()

abstract class ScreenMessage : Message()

val Navigation.screenId: ScreenId?
    get() = when (this) {
        is NavigateToArticleDetails -> screenId
        is NavigateToFavorite -> id
        is NavigateToFeed -> id
        is NavigateToSettings -> id
        is NavigateToTrending -> id
        Pop -> null
    }
