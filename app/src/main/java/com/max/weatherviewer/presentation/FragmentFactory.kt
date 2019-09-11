package com.max.weatherviewer.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.max.weatherviewer.presentation.map.MapFragment
import com.max.weatherviewer.presentation.viewer.WeatherViewerFragment
import org.kodein.di.Kodein

class FragmentsFactory(private val parent: Kodein) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {

            MapFragment::class.java -> MapFragment(parent)

            WeatherViewerFragment::class.java -> WeatherViewerFragment(parent)

            else -> super.instantiate(classLoader, className)
        }
    }

}