/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

package io.github.xlopec.reader.app.feature.filter

import io.github.xlopec.reader.app.FullScreen
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.misc.Loadable
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.Source
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

public typealias SourcesState = Loadable<Source>

public data class FiltersState(
    override val id: ScreenId,
    val filter: Filter,
    val sourcesState: SourcesState,
    val recentSearches: PersistentList<Query> = persistentListOf(),
) : FullScreen {
    internal companion object {
        const val StoreSuggestionsLimit = 10U
    }
}
