package com.example.disasterrelief.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.example.disasterrelief.viewmodel.SosViewModel
import com.example.disasterrelief.viewmodel.AuthViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.GeoPoint

@SuppressLint("MissingPermission") // We check permission manually
@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    sosViewModel: SosViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionState = remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Ask permission launcher
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionState.value = granted
    }

    // Check permission on first load
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        permissionState.value = granted
        if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // ✅ Create MapView safely
    val mapView = remember {
        try {
            MapView(context).apply {
                onCreate(null)
                onResume()
                MapsInitializer.initialize(context)
            }
        } catch (e: Exception) {
            Log.e("MAP_INIT", "Error creating MapView", e)
            null
        }
    }

    // ✅ UI Layout
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Home",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Display Google Map
            mapView?.let { mv ->
                AndroidView(
                    factory = { mv },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) { view ->
                    mv.getMapAsync { googleMap ->
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.uiSettings.isMyLocationButtonEnabled = true

                        if (permissionState.value) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val latLng = LatLng(location.latitude, location.longitude)
                                    googleMap.clear()
                                    googleMap.addMarker(
                                        MarkerOptions()
                                            .position(latLng)
                                            .title("You are here")
                                    )
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                    )
                                } else {
                                    Log.w("MAP_LOCATION", "Location is null")
                                }
                            }.addOnFailureListener {
                                Log.e("MAP_LOCATION", "Failed to get location: ${it.message}")
                            }
                        }
                    }
                }
            } ?: Text(
                "❌ Error loading map. Check your API key or internet connection.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        // ✅ SOS Floating Action Button
        FloatingActionButton(
            onClick = {
                val uid = authViewModel.currentUserId() ?: return@FloatingActionButton

                if (permissionState.value) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            sosViewModel.createSosRequest(uid, geoPoint) { success, error ->
                                message = if (success)
                                    "✅ SOS sent successfully!"
                                else
                                    error ?: "❌ Failed to send SOS"
                            }
                        } else {
                            message = "⚠️ Unable to get location. Try again."
                        }
                    }.addOnFailureListener {
                        message = "❌ Error fetching location: ${it.message}"
                    }
                } else {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("SOS")
        }

        // ✅ Feedback message
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (message.startsWith("✅"))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}
