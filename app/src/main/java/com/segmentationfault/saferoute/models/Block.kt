package com.segmentationfault.saferoute.models

import com.google.gson.annotations.SerializedName

data class Block (
        @SerializedName("latidute") val latitude: Double,
        @SerializedName("longitude") val longitude: Double,
        @SerializedName("dense") val dense: Float,
        @SerializedName("sparse") val sparse: Float,
        @SerializedName("username") val username: String
)