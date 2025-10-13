package com.example.disasterrelief.ui.screens.sos

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disasterrelief.data.service.LocationService
import com.example.disasterrelief.viewmodel.SosViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SosRequestScreen(
    navController: NavController,
    sosViewModel: SosViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val victimId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var message by remember { mutableStateOf("") }

    // Launcher to request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationService.getCurrentLocation { geoPoint ->
                if (geoPoint != null) {
                    sosViewModel.createSosRequest(victimId, geoPoint) { success, error ->
                         if (success) {
                             message = "✅ SOS sent with location!"
                             navController.navigate("victim_sos_status") {
                                 popUpTo("sos_request") { inclusive = true }
                             }
                         }
                         else {
                            error ?: "❌ Error while sending SOS"
                        }
                    }
                } else {
                    message = "⚠️ Unable to fetch location"
                }
            }
        } else {
            message = "⚠️ Location permission denied"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send SOS with Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (message.startsWith("✅")) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }
    }
}
