package com.example.disasterrelief.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.example.disasterrelief.viewmodel.SosViewModel
import com.example.disasterrelief.viewmodel.AuthViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint

@SuppressLint("MissingPermission") // We'll handle permission manually
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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        permissionState.value = isGranted
    }

    // Check permission when screen loads
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        permissionState.value = granted
        if (!granted) launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Home", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Map goes here (MapScreen will be embedded).")
        }

        FloatingActionButton(
            onClick = {
                val uid = authViewModel.currentUserId() ?: return@FloatingActionButton

                if (permissionState.value) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val geoPoint = com.google.firebase.firestore.GeoPoint(location.latitude, location.longitude)
                            sosViewModel.createSosRequest(uid, geoPoint) { success, error ->
                                message = if (success) "✅ SOS sent successfully!" else error ?: "❌ Failed to send SOS"
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

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (message.startsWith("✅")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}
