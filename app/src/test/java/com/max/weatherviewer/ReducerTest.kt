package com.max.weatherviewer

import com.max.weatherviewer.model.Location
import com.max.weatherviewer.model.Weather
import com.max.weatherviewer.model.Wind
import org.junit.Assert.assertTrue
import org.junit.Test

class ReducerTest {

    @Test
    fun testReducer() {
        assertTrue(reduce(State.Loading, InternalAction.FeedLoading) == State.Loading)

        val th = RuntimeException("foo")

        assertTrue(reduce(State.Loading, InternalAction.FeedLoadFailure(th)) == State.Failure(th))

        val weather = Weather(Location(30.0, 30.0), Wind(10.0, 30.0))

        assertTrue(reduce(State.Loading, InternalAction.FeedLoaded(weather)) == State.Preview(weather))
    }
}