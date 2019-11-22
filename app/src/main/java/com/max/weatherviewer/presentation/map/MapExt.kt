package com.max.weatherviewer.presentation.map

/*
fun Marker.moveAnimated(to: LatLng, duration: ULong) {
    (tag as? ObjectAnimator)?.cancel()

    val animator = ObjectAnimator.ofObject(MarkerEvaluator, position, to)
        .setDuration(duration.toLong())
        .apply { addUpdateListener { position = it.animatedValue as LatLng } }

    tag = animator
    animator.start()
}

@Suppress("DEPRECATION")// rot email
fun GoogleMap.cameraChanges(): Flow<Message.UpdateCamera> {
    return channelFlow {
        val l = GoogleMap.OnCameraChangeListener { position ->
            offer(Message.UpdateCamera(Location(position.target.latitude, position.target.longitude), position.zoom, position.bearing, position.tilt))
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
}

*/
/*suspend fun SupportMapFragment.googleMap(): GoogleMap {
    return suspendCoroutine { c -> getMapAsync { map -> c.resume(map) } }
}

fun marker(latLng: LatLng): MarkerOptions = MarkerOptions().position(latLng)*//*


private object MarkerEvaluator :
    TypeEvaluator<LatLng> {

    override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
        return LatLng(startValue.latitude + fraction * (endValue.latitude - startValue.latitude),
            startValue.longitude + fraction * (endValue.longitude - startValue.longitude))
    }
}*/
