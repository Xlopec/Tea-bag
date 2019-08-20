package com.max.weatherviewer.presentation.map

import com.max.weatherviewer.api.weather.Location
import org.junit.Assert
import org.junit.Test

class MapComponentKtTest {

    @Test
    fun update() {

        val expectedLoc = Location(10.0, 10.0)

        val (state, _) = update(Message.MoveTo(expectedLoc), State(Location(20.0, 30.0)))

        Assert.assertTrue(state == State(expectedLoc))
    }
}