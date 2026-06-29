/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.async

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

private sealed interface PageError {
    data object Timeout : PageError
    data class Server(val code: Int) : PageError
}

class PaginatableTest {

    @Test
    fun loadingListStartsEmpty() {
        val p = Paginatable.loadingList<String>()

        assertEquals(persistentListOf(), p.data)
        assertFalse(p.hasMore)
        assertTrue(p.isLoading)
    }

    @Test
    fun idleListAcceptsInitialData() {
        val p = Paginatable.idleList(persistentListOf("a", "b"))

        assertEquals(persistentListOf("a", "b"), p.data)
        assertTrue(p.isIdle)
        assertTrue(p.isRefreshable)
    }

    @Test
    fun isIdleAlsoTrueInExceptionState() {
        val p: Paginatable<String, PageError> =
            Paginatable.idleList(persistentListOf("x")).toException(PageError.Timeout)

        assertTrue(p.isIdle, "isIdle is intended to mean 'a new request may start'")
        assertTrue(p.isException)
        assertTrue(p.isRefreshable)
    }

    @Test
    fun toLoadingClearsPayload() {
        val p = Paginatable.idleList(persistentListOf("a", "b")).toLoading()

        assertEquals(persistentListOf(), p.data)
        assertTrue(p.isLoading)
    }

    @Test
    fun toLoadingNextOnEmptyDataThrows() {
        val p = Paginatable.idleList<String>()

        assertFails { p.toLoadingNext() }
    }

    @Test
    fun toLoadingNextOnNonEmptyDataSucceeds() {
        val p = Paginatable.idleList(persistentListOf("a")).toLoadingNext()

        assertTrue(p.isLoadingNext)
        assertEquals(persistentListOf("a"), p.data)
    }

    @Test
    fun toRefreshingKeepsExistingItems() {
        val p = Paginatable.idleList(persistentListOf("a", "b")).toRefreshing()

        assertEquals(persistentListOf("a", "b"), p.data)
        assertTrue(p.isRefreshing)
    }

    @Test
    fun toIdlePageFromLoadingReplacesItems() {
        val p = Paginatable.loadingList<String>().toIdle(Page(persistentListOf("a", "b"), hasMore = true))

        assertEquals(persistentListOf("a", "b"), p.data)
        assertTrue(p.hasMore)
        assertTrue(p.isIdle)
    }

    @Test
    fun toIdlePageFromRefreshingReplacesItems() {
        val p = Paginatable.idleList(persistentListOf("stale"))
            .toRefreshing()
            .toIdle(Page(persistentListOf("fresh"), hasMore = false))

        assertEquals(persistentListOf("fresh"), p.data)
        assertFalse(p.hasMore)
        assertTrue(p.isIdle)
    }

    @Test
    fun toIdlePageFromLoadingNextAppendsItems() {
        val p = Paginatable.idleList(persistentListOf("a"))
            .toLoadingNext()
            .toIdle(Page(persistentListOf("b", "c"), hasMore = true))

        assertEquals(persistentListOf("a", "b", "c"), p.data)
        assertTrue(p.hasMore)
        assertTrue(p.isIdle)
    }

    @Test
    fun toIdlePageFromExceptionAppendsItems() {
        val p: Paginatable<String, PageError> = Paginatable.idleList(persistentListOf("a"))
            .toException(PageError.Server(500))
            .toIdle(Page(persistentListOf("b"), hasMore = false))

        assertEquals(persistentListOf("a", "b"), p.data)
        assertFalse(p.hasMore)
        assertTrue(p.isIdle)
    }

    @Test
    fun toIdlePageWithHasMoreFalseAppendsAndStops() {
        val p = Paginatable.idleList(persistentListOf("a"))
            .toLoadingNext()
            .toIdle(Page(persistentListOf("b"), hasMore = false))

        assertEquals(persistentListOf("a", "b"), p.data)
        assertFalse(p.hasMore)
    }

    @Test
    fun canLoadNextForIndexRequiresLastIndexAndHasMore() {
        val p = Paginatable.idleList(persistentListOf("a", "b"))
            .toLoadingNext()
            .toIdle(Page(persistentListOf("c"), hasMore = true))

        assertTrue(p.hasMore)
        assertTrue(p.canLoadNextForIndex(p.data.lastIndex))
        assertFalse(p.canLoadNextForIndex(0))
    }

    @Test
    fun canLoadNextForIndexFalseWhenNoMore() {
        val p = Paginatable.idleList(persistentListOf("a"))

        assertFalse(p.hasMore)
        assertFalse(p.canLoadNextForIndex(p.data.lastIndex))
    }

    @Test
    fun canLoadNextForItemMatchesLastItem() {
        val p = Paginatable.loadingList<String>()
            .toIdle(Page(persistentListOf("a", "b"), hasMore = true))

        assertTrue(p.canLoadNextForItem("b"))
        assertFalse(p.canLoadNextForItem("a"))
        assertFalse(p.canLoadNextForItem(null))
    }

    @Test
    fun dataApplyTransformsList() {
        val p = Paginatable.idleList(persistentListOf("a")).data { adding("b") }

        assertEquals(persistentListOf("a", "b"), p.data)
    }

    @Test
    fun statelessObjectsAreSingletons() {
        val a = Paginatable.idleList<String>()
        val b = Paginatable.idleList<Int>()

        assertSame(a.state, b.state)
    }

    @Test
    fun exceptionRetainsTypedErrorThroughPatternMatching() {
        val p: Paginatable<String, PageError> =
            Paginatable.idleList(persistentListOf("a")).toException(PageError.Server(502))

        val message: String = when (val state = p.state) {
            is Paginatable.Exception -> when (val err = state.error) {
                PageError.Timeout -> "timeout"
                is PageError.Server -> "server-${err.code}"
            }
            Paginatable.Idle, Paginatable.Loading, Paginatable.LoadingNext, Paginatable.Refreshing -> "running"
        }

        assertEquals("server-502", message)

        val state = p.state
        assertIs<Paginatable.Exception<PageError>>(state)
    }
}
