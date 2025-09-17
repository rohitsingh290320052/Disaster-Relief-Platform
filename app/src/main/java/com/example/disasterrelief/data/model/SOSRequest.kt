package com.example.disasterrelief.data.model

import com.google.firebase.firestore.GeoPoint

data class SOSRequest(
    val id: String = "",
    val victimId: String = "",
    val location: GeoPoint? = null,
    val status: String = "open", // open / assigned / closed
    val timestamp: Long = System.currentTimeMillis()
)
