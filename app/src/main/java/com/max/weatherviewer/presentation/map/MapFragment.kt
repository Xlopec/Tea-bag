package com.max.weatherviewer.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxrelay2.PublishRelay
import com.max.weatherviewer.R
import com.max.weatherviewer.api.weather.Location
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class MapFragment : Fragment(), KodeinAware {

    override val kodein: Kodein = Kodein.lazy {
        val parent by closestKodein()

        extend(parent)
        import(mapModule(this@MapFragment))
    }

    private val component: MapComponent by instance("map")

    private val mapFragment: SupportMapFragment
        get() = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapFragment.getMapAsync { map ->

            val marker = map.addMarker(MarkerOptions().position(map.cameraPosition.target))
            val selections = view.findViewById<FloatingActionButton>(R.id.btn_select).clicks().map { Message.Select }

            disposable += component(map.positionChanges.map(Message::MoveTo).cast(Message::class.java).mergeWith(selections))
                .subscribe { state -> render(state, map, marker) }
        }
    }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    private fun render(state: State, map: GoogleMap, marker: Marker) {
        marker.move(map.cameraPosition)
        println(state.toString())
    }

}

private val LatLng.location: Location
    get() = Location(latitude, longitude)

private fun Marker.move(position: CameraPosition) {
    this.position = position.target
}

private val GoogleMap.positionChanges: Observable<Location>
    get() {
        val locations = PublishRelay.create<Location>()

        setOnCameraChangeListener { position -> locations.accept(position.target.location) }

        return locations
    }