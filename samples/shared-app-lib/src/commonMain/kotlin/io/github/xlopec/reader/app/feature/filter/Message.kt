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

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.ScreenMessage
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.Source
import io.github.xlopec.reader.app.model.SourceId
import kotlinx.collections.immutable.ImmutableList
import kotlin.jvm.JvmInline

public sealed interface FilterMessage : ScreenMessage {
    public val id: ScreenId
}

@JvmInline
public value class LoadSources(
    override val id: ScreenId,
) : FilterMessage

@JvmInline
public value class ClearSelection(
    override val id: ScreenId,
) : FilterMessage

public data class ToggleSourceSelection(
    override val id: ScreenId,
    val sourceId: SourceId,
) : FilterMessage

public data class RecentSearchesLoaded(
    override val id: ScreenId,
    val suggestions: ImmutableList<Query>,
) : FilterMessage

public data class RecentSearchRemoved(
    override val id: ScreenId,
    val search: Query,
) : FilterMessage

public sealed interface SourcesLoadResult : FilterMessage

public data class SourcesLoadSuccess(
    override val id: ScreenId,
    val sources: ImmutableList<Source>,
) : SourcesLoadResult

public data class SourcesLoadException(
    override val id: ScreenId,
    val exception: AppException,
) : SourcesLoadResult
