package com.max.weatherviewer.presentation.map

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.presentation.map.google.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@UseExperimental(ExperimentalUnsignedTypes::class)
fun Marker.moveAnimated(to: LatLng, duration: ULong) {
    (tag as? ObjectAnimator)?.cancel()

    val animator = ObjectAnimator.ofObject(MarkerEvaluator, position, to)
        .setDuration(duration.toLong())
        .apply { addUpdateListener { position = it.animatedValue as LatLng } }

    tag = animator
    animator.start()
}

val LatLng.location: Location
    get() = Location(latitude, longitude)

@Suppress("DEPRECATION")// rot email
val GoogleMap.cameraChanges: Flow<Message.UpdateCamera>
    get() = channelFlow {
        val l = GoogleMap.OnCameraChangeListener { position ->
            offer(Message.UpdateCamera(position.target.location, position.zoom, position.bearing, position.tilt))
        }

        setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                setOnCameraChangeListener(l)
            } else {
                setOnCameraChangeListener(null)
            }
        }

        awaitClose { setOnCameraChangeListener(null); setOnCameraMoveStartedListener(null) }
    }

suspend fun SupportMapFragment.googleMap(): GoogleMap {
    return suspendCoroutine { c -> getMapAsync { map -> c.resume(map) } }
}

fun marker(latLng: LatLng): MarkerOptions = MarkerOptions().position(latLng)

private object MarkerEvaluator :
    TypeEvaluator<LatLng> {

    override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
        return LatLng(startValue.latitude + fraction * (endValue.latitude - startValue.latitude),
                      startValue.longitude + fraction * (endValue.longitude - startValue.longitude))
    }
}