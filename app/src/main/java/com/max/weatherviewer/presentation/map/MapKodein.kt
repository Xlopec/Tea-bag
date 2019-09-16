package com.max.weatherviewer.presentation.map

import androidx.fragment.app.Fragment
import com.max.weatherviewer.args
import com.max.weatherviewer.presentation.map.geodecoder.geocoderModule
import com.max.weatherviewer.presentation.map.google.mapModule
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import kotlin.reflect.KProperty

class MapKodein(private val parent: Kodein) : KodeinAware {

    override lateinit var kodein: Kodein

    operator fun getValue(thisRef: Fragment, property: KProperty<*>): Kodein {

        if (!::kodein.isInitialized) {
            kodein = Kodein.lazy {
                extend(parent)

                import(geocoderModule(thisRef))
                import(mapModule(thisRef,
                                 thisRef.args<MapFragmentArgs>().preSelectedLocation))
            }
        }

        return kodein
    }

}