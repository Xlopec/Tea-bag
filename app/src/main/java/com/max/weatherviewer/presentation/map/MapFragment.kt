package com.max.weatherviewer.presentation.map

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
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
        import(mapModule(this@MapFragment, arguments?.let(MapFragmentArgs::fromBundle)?.preSelectedLocation))
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

            disposable += component(map.locChanges.map(Message::MoveTo).cast(Message::class.java).mergeWith(selections))
                .subscribe { state -> render(state, map, marker) }
        }
    }

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    private fun render(state: State, map: GoogleMap, marker: Marker) {
        val latLng = LatLng(state.location.lat, state.location.lon)

        map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder(map.cameraPosition).target(latLng).build()))
        marker.moveAnimated(latLng, resources.getInteger(android.R.integer.config_mediumAnimTime).toULong())
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
private val GoogleMap.locChanges: Observable<Location>
    get() {
        val locations = PublishRelay.create<Location>()

        val l = GoogleMap.OnCameraChangeListener { position -> locations.accept(position.target.location) }

        setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                setOnCameraChangeListener(l)
            } else {
                setOnCameraChangeListener(null)
            }
        }

        return locations
    }

private object MarkerEvaluator : TypeEvaluator<LatLng> {

    override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
        return LatLng(startValue.latitude + fraction * (endValue.latitude - startValue.latitude),
                      startValue.longitude + fraction * (endValue.longitude - startValue.longitude))
    }
}