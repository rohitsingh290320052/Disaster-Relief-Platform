package com.example.disasterrelief.data.model

import com.google.firebase.firestore.GeoPoint

data class Resource(
    val id: String = "",
    val ngoId: String = "",
    val type: String = "", // food, water, medical
    val quantity: Int = 0,
    val location: GeoPoint? = null
)
