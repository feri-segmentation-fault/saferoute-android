package com.segmentationfault.saferoute.models

import com.google.gson.annotations.SerializedName

data class Acceleration (
    @SerializedName("time") val time: String,
    @SerializedName("accelerationAmount") val accelerationAmount: Float
)