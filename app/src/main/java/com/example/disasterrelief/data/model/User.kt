package com.example.disasterrelief.data.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val id: String = "",
    val name: String = "",
    val role: String = "", // victim / volunteer / ngo
    val phone: String = "",
    val location: GeoPoint? = null
)
