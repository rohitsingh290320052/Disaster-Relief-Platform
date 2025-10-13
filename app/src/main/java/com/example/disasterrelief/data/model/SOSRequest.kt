package com.example.disasterrelief.data.model

import com.google.firebase.firestore.GeoPoint

data class SOSRequest(
    val id: String = "",
    val victimId: String = "",
    val location: com.google.firebase.firestore.GeoPoint? = null,
    val status: String = "open",
    val assignedVolunteerId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

