package com.max.weatherviewer.di

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import kotlin.reflect.KProperty

class FragmentKodein<S>(private val setup: Kodein.MainBuilder.(s: S) -> Unit) : KodeinAware where S : Fragment,
                                                                                                  S : CoroutineScope {

    override lateinit var kodein: Kodein

    operator fun getValue(thisRef: S, property: KProperty<*>): Kodein {

        if (!::kodein.isInitialized) {
            kodein = Kodein.lazy { setup(thisRef) }
        }

        return kodein
    }

}