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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private sealed interface SampleError {
    data object NetworkDown : SampleError
    data class Http(val code: Int) : SampleError
}

class LoadableTest {

    @Test
    fun idleSingleProducesIdleState() {
        val loadable = Loadable.idleSingle("hi")

        assertEquals("hi", loadable.data)
        assertSame(Loadable.Idle, loadable.state)
        assertTrue(loadable.isIdle)
        assertTrue(loadable.isRefreshable)
        assertFalse(loadable.isLoading)
        assertFalse(loadable.isRefreshing)
        assertFalse(loadable.isException)
    }

    @Test
    fun loadingSingleNullableProducesNullData() {
        val loadable: Loadable<String?, Nothing> = Loadable.loadingSingle()

        assertNull(loadable.data)
        assertTrue(loadable.isLoading)
        assertFalse(loadable.isIdle)
    }

    @Test
    fun loadingSingleWithDataKeepsPlaceholder() {
        val loadable = Loadable.loadingSingle("stale")

        assertEquals("stale", loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun loadingListStartsEmpty() {
        val loadable = Loadable.loadingList<String>()

        assertEquals(persistentListOf(), loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun idleListAcceptsInitialData() {
        val loadable = Loadable.idleList(persistentListOf("a", "b"))

        assertEquals(persistentListOf("a", "b"), loadable.data)
        assertTrue(loadable.isIdle)
    }

    @Test
    fun isIdleAlsoTrueInExceptionState() {
        val loadable: Loadable<String, SampleError> =
            Loadable.idleSingle("x").toException(SampleError.NetworkDown)

        assertTrue(loadable.isIdle, "isIdle is intended to mean 'a new request may start'")
        assertTrue(loadable.isException)
        assertTrue(loadable.isRefreshable)
    }

    @Test
    fun toExceptionPreservesDataAndCarriesTypedError() {
        val loadable: Loadable<String, SampleError> =
            Loadable.idleSingle("payload").toException(SampleError.Http(503))

        assertEquals("payload", loadable.data)
        val state = loadable.state
        assertIs<Loadable.Exception<SampleError>>(state)
        assertEquals(SampleError.Http(503), state.error)
    }

    @Test
    fun toLoadingClearsListPayload() {
        val loadable = Loadable.idleList(persistentListOf("a", "b")).toLoading()

        assertEquals(persistentListOf(), loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun toLoadingClearsNullableSinglePayload() {
        val loadable: Loadable<String?, Nothing> = Loadable.idleSingle<String?>("a").toLoading()

        assertNull(loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun toRefreshingKeepsData() {
        val loadable = Loadable.idleSingle("keep").toRefreshing()

        assertEquals("keep", loadable.data)
        assertTrue(loadable.isRefreshing)
    }

    @Test
    fun toIdleWithDataReplacesPayload() {
        val loadable = Loadable.loadingSingle("old").toIdle("new")

        assertEquals("new", loadable.data)
        assertTrue(loadable.isIdle)
    }

    @Test
    fun toIdleListWithDataReplacesPayload() {
        val loadable = Loadable.loadingList<String>().toIdle(persistentListOf("x"))

        assertEquals(persistentListOf("x"), loadable.data)
        assertTrue(loadable.isIdle)
    }

    @Test
    fun dataAppliesUpdaterToSingle() {
        val loadable = Loadable.idleSingle("a").data { plus("b") }

        assertEquals("ab", loadable.data)
    }

    @Test
    fun dataListAppliesUpdaterToList() {
        val loadable = Loadable.idleList(persistentListOf(1, 2)).data { adding(3) }

        assertEquals(persistentListOf(1, 2, 3), loadable.data)
    }

    @Test
    fun loadingAndIdleAreSingletonsAcrossInstances() {
        val a = Loadable.idleSingle("a")
        val b = Loadable.idleSingle(42)

        assertSame(a.state, b.state, "Idle should be a shared object instance")
    }

    @Test
    fun exceptionRetainsTypedErrorThroughPatternMatching() {
        val loadable: Loadable<Int, SampleError> =
            Loadable.idleSingle(0).toException(SampleError.Http(404))

        val message: String = when (val state = loadable.state) {
            is Loadable.Exception -> when (val err = state.error) {
                SampleError.NetworkDown -> "down"
                is SampleError.Http -> "http-${err.code}"
            }
            Loadable.Idle, Loadable.Loading, Loadable.Refreshing -> "running"
        }

        assertEquals("http-404", message)
    }
}
