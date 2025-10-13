package com.example.disasterrelief.ui.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.disasterrelief.viewmodel.SosViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    navController: androidx.navigation.NavController,
    sosViewModel: SosViewModel = viewModel()
) {
    val context = LocalContext.current
    Log.d("MAP_KEY", "Key: ${context.getString(com.example.disasterrelief.R.string.google_maps_key)}")

    val mapView = rememberMapViewWithLifecycle() ?: return
    val sosRequests by sosViewModel.sosRequests.collectAsState()
    var selectedSosId by remember { mutableStateOf<String?>(null) }
    val volunteerId = FirebaseAuth.getInstance().currentUser?.uid

    // Handle location permission
    var hasLocationPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasLocationPermission = granted
    }
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { mapView ->
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.clear()

            // Enable my-location layer if permission granted
            var userLatLng: LatLng? = null
            if (hasLocationPermission) {
                googleMap.isMyLocationEnabled = true
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        userLatLng = LatLng(it.latitude, it.longitude)
                    }
                }
            }

            // Show open SOS requests
            val sosLatLngs = mutableListOf<LatLng>()
            sosRequests.filter { it.status == "open" }.forEach { sos ->
                sos.location?.let { loc ->
                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(loc.latitude, loc.longitude))
                            .title("SOS Request")
                    )
                    marker?.tag = sos.id
                    sosLatLngs.add(LatLng(loc.latitude, loc.longitude))
                }
            }

            // Include user location if available
            userLatLng?.let { sosLatLngs.add(it) }

            // Zoom map to include all markers
            if (sosLatLngs.isNotEmpty()) {
                val boundsBuilder = LatLngBounds.builder()
                sosLatLngs.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            }

            // Marker click
            googleMap.setOnMarkerClickListener { marker ->
                selectedSosId = marker.tag as? String
                true
            }
        }
    }

    // Accept SOS Dialog
    if (selectedSosId != null && volunteerId != null) {
        AlertDialog(
            onDismissRequest = { selectedSosId = null },
            title = { Text("Accept SOS Request") },
            text = { Text("Do you want to accept this SOS request and help?") },
            confirmButton = {
                Button(onClick = {
                    sosViewModel.assignSosToVolunteer(selectedSosId!!, volunteerId)
                    selectedSosId = null
                    navController.navigate("volunteer_assignments")
                }) {
                    Text("Accept")
                }
            },
            dismissButton = {
                Button(onClick = { selectedSosId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView? {
    val context = LocalContext.current
    val mapView = remember {
        try {
            MapView(context)
        } catch (e: Exception) {
            Log.e("MAP_INIT", "Error creating MapView", e)
            null
        }
    } ?: return null

    DisposableEffect(Unit) {
        mapView.onCreate(null)
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    return mapView
}
