package com.max.weatherviewer.presentation.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.ui.core.setContent
import com.max.weatherviewer.args
import com.max.weatherviewer.di.FragmentKodein
import com.max.weatherviewer.dispose
import com.max.weatherviewer.presentation.LifecycleAwareContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class WeatherViewerFragment(parent: Kodein) : Fragment(), KodeinAware, CoroutineScope {

    override val kodein by FragmentKodein { fragment ->
        extend(parent)
        import(fragment.weatherModule(fragment.args<WeatherViewerFragmentArgs>().location))
    }

    override val coroutineContext by LifecycleAwareContext()
    private val component by instance<WeatherComponent>()
    private val viewJobs = mutableListOf<Job>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FrameLayout(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewJobs += launch {
            val messages = Channel<Message>()

            component(messages.consumeAsFlow()).collect { state -> (view as ViewGroup).render(messages, state) }
        }
    }

    override fun onDestroyView() {
        viewJobs.dispose()
        super.onDestroyView()
    }
}

private fun ViewGroup.render(messages: Channel<Message>, state: State) = setContent { WeatherView(messages, state) }