package com.max.weatherviewer.api.weather

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//"b40db1b95c75e4668ab28ed46a6c6c45"
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