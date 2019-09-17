package com.max.weatherviewer.presentation.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.ui.core.setContent
import com.oliynick.max.elm.core.misc.startWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class WeatherViewerFragment(parent: Kodein) : Fragment(), KodeinAware, CoroutineScope by MainScope() {

    override val kodein by WeatherViewerKodein(parent)

    private val component by instance<WeatherComponent>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FrameLayout(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val messages = Channel<Message>(Channel.UNLIMITED)

        launch {

            component(messages.consumeAsFlow().startWith(Message.ViewAttached))
                .collect { state -> (view as ViewGroup).render(messages, state) }
        }

        /*launch {
            messages.consumeAsFlow().collect {
                println("LL $it")
            }
        }*/
    }

    override fun onDestroyView() {
        cancel()
        super.onDestroyView()
    }
}

private fun ViewGroup.render(messages: Channel<Message>, state: State) = setContent { WeatherView(messages, state) }