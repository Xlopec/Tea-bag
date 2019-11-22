package com.max.weatherviewer.presentation.main

import android.app.Activity
import android.os.Bundle
import androidx.ui.core.Text
import androidx.ui.core.setContent
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView

class MainActivity : Activity()/*, KodeinAware*/ {

    /*private val parentKodein by closestKodein()

    override val kodein: Kodein by retainedKodein {

        extend(parentKodein, allowOverride = true)
        import(locationModule())

        bind<Activity>(tag = Activity::class) with provider { this@MainActivity }
    }*/

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        //supportFragmentManager.fragmentFactory = FragmentsFactory(kodein)
        super.onCreate(savedInstanceState)

        mapView = MapView(this, GoogleMapOptions().zoomControlsEnabled(true).scrollGesturesEnabledDuringRotateOrZoom(true).scrollGesturesEnabled(true))

        mapView.onCreate(savedInstanceState)

        setContentView(mapView)

        mapView.setContent {
            Text("Hello")
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //direct.instance<PermissionPublisher>().offer(PermissionResult(requestCode, permissions, grantResults))
    }

}
