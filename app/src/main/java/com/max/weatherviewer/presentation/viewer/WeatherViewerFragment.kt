package com.max.weatherviewer.presentation.viewer

import com.max.weatherviewer.api.weather.Weather

private fun formatWeather(w: Weather): String {
    return "Weather for lat=%.2f, lon=%.2f: wind speed is %.2f of %.2f degrees"
        .format(w.location.lat, w.location.lon, w.wind.speed, w.wind.degrees)
}