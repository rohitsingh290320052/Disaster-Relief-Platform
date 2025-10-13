package com.example.disasterrelief.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.disasterrelief.ui.screens.auth.LoginScreen
import com.example.disasterrelief.ui.screens.auth.RegisterScreen
import com.example.disasterrelief.ui.screens.map.MapScreen
import com.example.disasterrelief.ui.screens.sos.SosRequestScreen
import com.example.disasterrelief.ui.screens.sos.VictimSosStatusScreen
import com.example.disasterrelief.ui.screens.sos.VolunteerAssignmentsScreen
import com.example.disasterrelief.viewmodel.AuthViewModel
import com.example.disasterrelief.viewmodel.SosViewModel

@Composable
fun AppNavGraph(authViewModel: AuthViewModel = viewModel(), sosViewModel: SosViewModel = viewModel()) {
    val navController = rememberNavController()
    var userRole by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasSosRequests by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = authViewModel.currentUserId()
        if (uid != null) {
            // Fetch role
            authViewModel.getUserRole { role ->
                userRole = role
                if (role == "victim") {
                    // Check if victim has any SOS requests
                    sosViewModel.getVictimSosRequests(uid) { list ->
                        hasSosRequests = list.isNotEmpty()
                        isLoading = false
                    }
                } else {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(navController, startDestination = if (userRole == null) "login" else "home") {

            // Auth screens
            composable("login") {
                LoginScreen(navController, authViewModel)
            }

            composable("register") {
                RegisterScreen(navController, authViewModel)
            }

            // Home / role-based screens
            composable("home") {
                when (userRole) {
                    "victim" -> {
                        if (hasSosRequests) {
                            VictimSosStatusScreen(navController, sosViewModel)
                        } else {
                            // No requests yet → go to SOS creation
                            SosRequestScreen(navController, sosViewModel)
                        }
                    }
                    "volunteer" -> VolunteerAssignmentsScreen(navController, sosViewModel)
                    "ngo" -> Text("NGO Screen Coming Soon")
                    else -> Text("Unknown role. Please re-register.")
                }
            }

            // Other screens
            composable("sos_request") { SosRequestScreen(navController, sosViewModel) }
            composable("victim_sos_status") { VictimSosStatusScreen(navController, sosViewModel) }
            composable("volunteer_assignments") { VolunteerAssignmentsScreen(navController, sosViewModel) }
            composable("map") { MapScreen(navController, sosViewModel) }
        }
    }
}

