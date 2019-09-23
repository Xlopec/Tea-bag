package com.max.weatherviewer.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KProperty

class LifecycleAwareContext : LifecycleObserver {

    private val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main

    operator fun <S : LifecycleOwner> getValue(thisRef: S, property: KProperty<*>): CoroutineContext {
        thisRef.lifecycle.addObserver(this)
        return coroutineContext
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun handleDestroy() {
        coroutineContext.cancel()
    }

}