package com.max.weatherviewer.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import org.kodein.di.bindings.Scope
import org.kodein.di.bindings.ScopeRegistry
import org.kodein.di.bindings.StandardScopeRegistry

val Fragment.fragmentScope: Scope<Fragment>
    get() {
        lifecycle.addObserver(FragmentScope)
        return FragmentScope
    }

private object FragmentScope : Scope<Fragment>, LifecycleObserver {

    private val mapping = mutableMapOf<Int, ScopeRegistry>()

    override fun getRegistry(context: Fragment): ScopeRegistry {
        return mapping.getOrPut(context.id, ::StandardScopeRegistry)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun handleDestroy(o: LifecycleOwner) {
        mapping.remove((o as Fragment).id)?.clear()
    }
}