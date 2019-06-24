package com.max.weatherviewer.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.max.weatherviewer.presentation.map.MapFragment

class FragmentsFactory : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String, args: Bundle?): Fragment {
        return when(loadFragmentClass(classLoader, className)) {
            MapFragment::class.java -> MapFragment()

            else -> super.instantiate(classLoader, className, args)
        }
    }

}