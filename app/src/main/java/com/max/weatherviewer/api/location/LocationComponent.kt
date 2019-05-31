package com.max.weatherviewer.api.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.jakewharton.rxrelay2.PublishRelay
import com.max.weatherviewer.api.location.LocationMessage.LocationResult
import com.max.weatherviewer.api.weather.Location
import com.max.weatherviewer.just
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import android.location.Location as AndroidLocation

typealias LocationPublisher = PublishRelay<Location>
typealias LocationObserver = Observable<Location>

typealias PermissionPublisher = PublishRelay<PermissionResult>
typealias PermissionObserver = Observable<PermissionResult>

typealias Permission = String

typealias LocationModel = (request: LocationRequest) -> Single<out LocationMessage>
typealias LibLocationProvider = ReactiveLocationProvider

private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
private const val LOCATION_REQUEST_ID = 123

data class PermissionResult(val requestId: Int, val permissions: Array<out String>, val grantResults: IntArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionResult

        if (requestId != other.requestId) return false
        if (!permissions.contentEquals(other.permissions)) return false
        if (!grantResults.contentEquals(other.grantResults)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestId
        result = 31 * result + permissions.contentHashCode()
        result = 31 * result + grantResults.contentHashCode()
        return result
    }
}

sealed class LocationMessage {
    object PermissionDenied : LocationMessage()
    object ShowRationale : LocationMessage()
    data class LocationResult(val l: Location) : LocationMessage()
}

class LocationComponent(private val locationProvider: LibLocationProvider,
                        private val permissionObserver: PermissionObserver,
                        private val activity: Activity) : LocationModel {

    override fun invoke(request: LocationRequest): Single<out LocationMessage> = activity.requestLocation(request)

    @SuppressLint("MissingPermission")
    private fun Activity.requestLocation(request: LocationRequest): Single<out LocationMessage> {

        if (isGranted(LOCATION_PERMISSION)) {
            return locationProvider.locationUpdates(request)
        }

        return doRequestLocation(request)
    }

    private fun Activity.doRequestLocation(request: LocationRequest): Single<LocationMessage> {

        return Completable.fromAction { requestPermission() }
            .andThen(permissionObserver)
            .filter { (requestId, _, _) -> requestId == LOCATION_REQUEST_ID }
            .firstOrError()
            .flatMap {
                when {
                    shouldShowRationale(LOCATION_PERMISSION) -> LocationMessage.ShowRationale.just()

                    isDenied(LOCATION_PERMISSION) -> LocationMessage.PermissionDenied.just()

                    else -> locationProvider.locationUpdates(request)
                }
            }
    }
}

@SuppressLint("MissingPermission")
fun LibLocationProvider.locationUpdates(request: LocationRequest): Single<LocationResult> {
    return getUpdatedLocation(request)
        .map(::toLocation)
        .map(::LocationResult)
        .firstOrError()
}

fun toLocation(l: AndroidLocation) = l.run { Location(latitude, longitude) }

private fun Context.isGranted(permission: Permission): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private fun Context.isDenied(permission: Permission): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED
}

private fun Activity.shouldShowRationale(permission: Permission): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

private fun Activity.requestPermission(requestId: Int = LOCATION_REQUEST_ID,
                                       vararg permissions: Permission = arrayOf(LOCATION_PERMISSION)) {

    ActivityCompat.requestPermissions(this, arrayOf(*permissions), requestId)
}
