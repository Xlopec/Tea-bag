package com.max.weatherviewer.presentation.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Weather
import com.max.weatherviewer.args
import com.max.weatherviewer.dispose
import com.max.weatherviewer.presentation.LifecycleAwareContext
import com.max.weatherviewer.safe
import kotlinx.android.synthetic.main.fragment_viewer.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class WeatherViewerFragment(parent: Kodein) : Fragment(), KodeinAware, CoroutineScope {

    override val kodein by Kodein.lazy {
        extend(parent)
        import(weatherModule(args<WeatherViewerFragmentArgs>().location))
    }

    override val coroutineContext by LifecycleAwareContext()
    private val component by instance<WeatherComponent>()
    private val viewJobs = mutableListOf<Job>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewJobs += launch {
            val messages = Channel<Message>()

            component(messages.consumeAsFlow()).collect { state -> render(state, messages) }
        }
    }

    override fun onDestroyView() {
        viewJobs.dispose()
        super.onDestroyView()
    }

    private fun render(state: State, messages: SendChannel<Message>) {
        when (state) {
            is State.Loading -> render(state)
            is State.Preview -> render(state, messages)
            is State.LoadFailure -> render(state, messages)
        }.safe
    }

    private fun render(state: State.Loading) {
        tv_message.text = "Loading"
        btn_action.isEnabled = false
    }

    private fun render(state: State.Preview, messages: SendChannel<Message>) {
        tv_message.text = formatWeather(state.data)
        btn_action.isEnabled = true
        btn_action.setOnClickListener { messages.offer(Message.SelectLocation) }
    }

    private fun render(state: State.LoadFailure, messages: SendChannel<Message>) {
        tv_message.text = "Failed to perform action ${state.th.localizedMessage}"
        btn_action.isEnabled = true
        btn_action.setOnClickListener { messages.offer(Message.Retry) }
    }

}

private fun formatWeather(w: Weather): String {
    return "Weather for lat=%.2f, lon=%.2f: wind speed is %.2f of %.2f degrees"
        .format(w.location.lat, w.location.lon, w.wind.speed, w.wind.degrees)
}