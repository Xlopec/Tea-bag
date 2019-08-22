package com.max.weatherviewer.presentation.viewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.max.weatherviewer.R
import com.max.weatherviewer.di.fragmentScope
import com.max.weatherviewer.presentation.start.Message
import com.max.weatherviewer.presentation.start.State
import com.max.weatherviewer.presentation.start.WeatherComponent
import com.max.weatherviewer.presentation.start.weatherModule
import com.max.weatherviewer.startWith
import kotlinx.android.synthetic.main.weather_viewer_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

class WeatherViewerFragment : Fragment(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        val parent by closestKodein()

        extend(parent)

        bind<Fragment>() with scoped(fragmentScope).singleton { this@WeatherViewerFragment }

        import(weatherModule(fragmentScope, navArgs<WeatherViewerFragmentArgs>().value.location))
    }

    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val component by instance<WeatherComponent>()

    private var snackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.weather_viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.inflateMenu(R.menu.weather_viewer)

        val messages = Channel<Message>(Channel.RENDEZVOUS)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_map -> messages.offer(Message.SelectLocation)
            }

            true
        }

        uiScope.launch {
            component(messages.consumeAsFlow().startWith(Message.ViewAttached))
                .collect { render(messages, it) }
        }
    }

    override fun onDestroyView() {
        uiScope.cancel()
        super.onDestroyView()
    }

    private fun render(relay: SendChannel<Message>, state: State) {
        snackbar?.dismiss()

        Log.d(javaClass.simpleName, "State: $state")

        when (state) {
            is State.Loading -> render()
            is State.Preview -> render(state)
            is State.Initial -> Unit
            is State.LoadFailure -> render(relay, state)
            is State.PermissionRequestFuckup -> TODO()
            is State.ShowPermissionRationale -> TODO()
            is State.RequestPermission -> TODO()
        }.safe
    }

    private fun render() {
        progressBar.isVisible = true
        tv_weather.isVisible = false
    }

    private fun render(state: State.Preview) {
        progressBar.isVisible = false
        tv_weather.isVisible = true

        tv_weather.text = state.data.let { w ->
            "Weather for lat=%.2f, lon=%.2f: wind speed is %.2f of %.2f degrees"
                .format(w.location.lat, w.location.lon, w.wind.speed, w.wind.degrees)
        }
    }

    private fun render(relay: SendChannel<Message>, state: State.LoadFailure) {
        progressBar.isVisible = false
        tv_weather.isVisible = false

        snackbar =
            Snackbar.make(view!!,
                          "Failed to perform action ${state.th.localizedMessage}",
                          Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry") { relay.offer(Message.Retry) }
                .also { it.show() }
    }

}

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this