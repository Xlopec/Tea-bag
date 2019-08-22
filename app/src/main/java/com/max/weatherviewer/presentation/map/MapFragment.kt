package com.max.weatherviewer.presentation.map

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.satoshun.coroutinebinding.view.clicks
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.mergeWith
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.flow.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance


class MapFragment : Fragment(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        val parent by closestKodein()

        extend(parent)
        import(mapModule(this@MapFragment,
                         arguments?.let(MapFragmentArgs::fromBundle)?.preSelectedLocation))
    }

    private val component: MapComponent by instance("map")

    private val mapFragment: SupportMapFragment
        get() = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapFragment.getMapAsync { map ->

            val marker = map.addMarker(MarkerOptions().position(map.cameraPosition.target))
            val selections = view.findViewById<FloatingActionButton>(R.id.btn_select).clicks()
                .map { Message.Select }

            uiScope.launch {
                component(map.locChanges.map { Message.MoveTo(it) }.mergeWith(selections.consumeAsFlow()))
                    .collect { state -> render(state, map, marker) }
            }
        }
    }

    override fun onDestroyView() {
        uiScope.cancel()
        super.onDestroyView()
    }

    private fun render(state: State, map: GoogleMap, marker: Marker) {
        val latLng = LatLng(state.location.lat, state.location.lon)

        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder(map.cameraPosition).target(
            latLng).build()))
        marker.moveAnimated(latLng,
                            resources.getInteger(android.R.integer.config_mediumAnimTime).toULong())
    }

}

@UseExperimental(ExperimentalUnsignedTypes::class)
private fun Marker.moveAnimated(to: LatLng, duration: ULong) {
    (tag as? ObjectAnimator)?.cancel()

    val animator = ObjectAnimator.ofObject(MarkerEvaluator, position, to)
        .setDuration(duration.toLong())
        .apply { addUpdateListener { position = it.animatedValue as LatLng } }

    tag = animator
    animator.start()
}

private val LatLng.location: Location
    get() = Location(latitude, longitude)

@Suppress("DEPRECATION")// rot email
private val GoogleMap.locChanges: Flow<Location>
    get() = channelFlow {
        val l = GoogleMap.OnCameraChangeListener { position -> offer(position.target.location) }

        setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                setOnCameraChangeListener(l)
            } else {
                setOnCameraChangeListener(null)
            }
        }

        awaitClose { setOnCameraChangeListener(null); setOnCameraMoveStartedListener(null) }
    }

private object MarkerEvaluator : TypeEvaluator<LatLng> {

    override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
        return LatLng(startValue.latitude + fraction * (endValue.latitude - startValue.latitude),
                      startValue.longitude + fraction * (endValue.longitude - startValue.longitude))
    }
}