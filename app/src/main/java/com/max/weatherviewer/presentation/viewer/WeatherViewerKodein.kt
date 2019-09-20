package com.max.weatherviewer.presentation.viewer

import androidx.fragment.app.Fragment
import com.max.weatherviewer.args
import com.max.weatherviewer.di.fragmentScope
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton
import kotlin.reflect.KProperty

class WeatherViewerKodein(private val parent: Kodein) : KodeinAware {

    override lateinit var kodein: Kodein

    operator fun <S> getValue(thisRef: S, property: KProperty<*>): Kodein where S : Fragment,
                                                                                S : CoroutineScope{

        if (!::kodein.isInitialized) {
            kodein = Kodein.lazy {
                extend(parent)

                bind<Fragment>() with scoped(thisRef.fragmentScope).singleton { thisRef as Fragment }

                import(weatherModule(thisRef.fragmentScope,
                                     thisRef.args<WeatherViewerFragmentArgs>().location, thisRef))
            }
        }

        return kodein
    }

}