package com.max.weatherviewer.presentation.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.max.weatherviewer.R
import com.max.weatherviewer.presentation.start.State
import com.max.weatherviewer.presentation.start.WeatherComponent
import com.max.weatherviewer.presentation.start.weatherModule
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WeatherViewerFragment : Fragment(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        val parent by closestKodein()

        extend(parent)
        import(weatherModule(navArgs<WeatherViewerFragmentArgs>().value.location))
    }

    private val state by instance<WeatherComponent>()
    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.weather_viewer_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val l = navArgs<WeatherViewerFragmentArgs>().value.location
        Toast.makeText(requireContext(), l.toString(), Toast.LENGTH_SHORT).show()

        disposable += state(Observable.never()).subscribe(::render)
    }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    private fun render(state: State) {
        System.out.println(state)
        //findViewById<TextView>(R.id.textView).text = state.toString()
    }

    private fun render(state: State.Loading) {
        Toast.makeText(requireContext(), state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Preview) {
        Toast.makeText(requireContext(), state.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun render(state: State.Failure) {
        Toast.makeText(requireContext(), state.toString(), Toast.LENGTH_SHORT).show()
    }

}