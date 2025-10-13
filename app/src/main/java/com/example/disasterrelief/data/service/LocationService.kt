package com.example.disasterrelief.data.service

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint

class LocationService(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationResult: (GeoPoint?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationResult(GeoPoint(location.latitude, location.longitude))
                } else {
                    onLocationResult(null)
                }
            }
            .addOnFailureListener {
                onLocationResult(null)
            }
    }
}
