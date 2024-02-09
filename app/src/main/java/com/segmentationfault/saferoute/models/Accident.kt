package com.segmentationfault.saferoute.models

import com.google.gson.annotations.SerializedName

data class Accident (
    @SerializedName("id") val id: Int,
    @SerializedName("dateTime") val dateTime: String,
    @SerializedName("coordinatesX") val latitude: Double,
    @SerializedName("coordinatesY") val longitude: Double,
    @SerializedName("username") val username: String,
    @SerializedName("text") val description: String,
    @SerializedName("accidentType") val accidentType: String,
    @SerializedName("photoPath") val photoPath: String,
    @SerializedName("photoB64") val photoB64: String
)
