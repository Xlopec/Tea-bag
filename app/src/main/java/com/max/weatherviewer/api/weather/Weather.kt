package com.max.weatherviewer.api.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wind(@SerializedName("speed") val speed: Double,
                @SerializedName("deg") val degrees: Double) : Parcelable

@Parcelize
data class Location(@SerializedName("lat") val lat: Double,
                    @SerializedName("lon", alternate = ["lng"]) val lon: Double) : Parcelable

@Parcelize
data class Weather(@SerializedName("coord") val location: Location,
                   @SerializedName("wind") val wind: Wind
) : Parcelable