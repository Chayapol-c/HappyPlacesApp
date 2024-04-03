package com.example.happyplaces.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HappyPlaceModel (
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable