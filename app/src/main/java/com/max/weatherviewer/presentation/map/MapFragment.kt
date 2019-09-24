package com.max.weatherviewer.presentation.map

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
import com.max.weatherviewer.*
import com.max.weatherviewer.presentation.LifecycleAwareContext
import com.max.weatherviewer.presentation.map.geodecoder.GeodecoderComponent
import com.max.weatherviewer.presentation.map.geodecoder.Preview
import com.max.weatherviewer.presentation.map.geodecoder.geocoderModule
import com.max.weatherviewer.presentation.map.google.MapComponent
import com.max.weatherviewer.presentation.map.google.Message
import com.max.weatherviewer.presentation.map.google.mapModule
import com.oliynick.max.elm.core.component.changes
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import com.max.weatherviewer.presentation.map.geodecoder.State as GeoState
import com.max.weatherviewer.presentation.map.google.State as MapState

class MapFragment(parent: Kodein) : Fragment(), KodeinAware, CoroutineScope {

    override val kodein: Kodein by Kodein.lazy {
        extend(parent)

        import(geocoderModule())
        import(mapModule(args<MapFragmentArgs>().preSelectedLocation))
    }

    override val coroutineContext by LifecycleAwareContext()
    private val mapComponent: MapComponent by instance("map")
    private val geoComponent: GeodecoderComponent by instance()
    private val viewJobs = mutableListOf<Job>()

    private val mapFragment by lazy {
        childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewJobs += launch {
            val map = mapFragment.googleMap()
            val marker = map.addMarker(marker(map.cameraPosition.target))
            val selections = btn_select.clicks().map { Message.Select }

            mapComponent(map.cameraChanges.mergeWith(selections.consumeAsFlow()))
                .collect { state -> render(state, map, marker) }
        }

        viewJobs += launch {
            geoComponent.changes().collect { render(it) }
        }
    }

    override fun onDestroyView() {
        viewJobs.dispose()
        super.onDestroyView()
    }

    private fun render(state: GeoState) {
        when (state) {
            is Preview -> tv_manual_location.setText(state.address)
        }.safe
    }

    private fun render(state: MapState, map: GoogleMap, marker: Marker) {
        val latLng = state.location.run { LatLng(lat, lon) }

        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder(map.cameraPosition)
                                                                    .target(latLng).zoom(state.zoom).tilt(state.tilt).bearing(state.bearing).build()))
        marker.moveAnimated(latLng,
                            resources.getInteger(android.R.integer.config_mediumAnimTime).toULong())
    }

}