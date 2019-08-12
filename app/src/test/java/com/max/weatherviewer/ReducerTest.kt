package com.max.weatherviewer

import com.max.weatherviewer.presentation.start.Command
import com.max.weatherviewer.presentation.start.State
import com.max.weatherviewer.presentation.reduce
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.api.weather.Weather
import com.max.weatherviewer.api.weather.Wind
import org.junit.Assert.assertTrue
import org.junit.Test

class ReducerTest {

    @Test
    fun testReducer() {
        assertTrue(
            reduce(
                State.Loading,
                Command.LoadWeather
            ) == State.Loading)

        val th = RuntimeException("foo")

        assertTrue(
            reduce(
                State.Loading,
                Command.FeedLoadFailure(th)
            ) == State.LoadFailure(th))

        val weather = Weather(Location(30.0, 30.0), Wind(10.0, 30.0))

        assertTrue(
            reduce(
                State.Loading,
                Command.FeedLoaded(weather)
            ) == State.Preview(weather))
    }
}