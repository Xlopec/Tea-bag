package com.max.weatherviewer.presentation.map

import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.presentation.map.google.Message
import com.max.weatherviewer.presentation.map.google.State
import org.junit.Assert
import org.junit.Test

class MapComponentImplKtTest {

    @Test
    fun update() {

        val expectedLoc = Location(10.0, 10.0)

        val (state, _) = com.max.weatherviewer.presentation.map.google.update(Message.UpdateCamera(
            expectedLoc), State(Location(20.0, 30.0)))

        Assert.assertTrue(state == State(expectedLoc))
    }
}