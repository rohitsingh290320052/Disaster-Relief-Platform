package com.example.disasterrelief.ui.screens.sos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.disasterrelief.data.model.SOSRequest
import com.example.disasterrelief.viewmodel.SosViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VolunteerAssignmentsScreen(
    navController: NavController,
    sosViewModel: SosViewModel = viewModel()
) {
    val volunteerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val sosRequests by sosViewModel.sosRequests.collectAsState()

    val activeRequests = sosRequests.filter { it.status != "closed" && it.status != "cancelled" }
    val availableRequests = activeRequests.filter { it.assignedVolunteerId == null }
    val myAssignments = activeRequests.filter { it.assignedVolunteerId == volunteerId }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("login") {
                    popUpTo(0)
                }
            }
        ) {
            Text("← Back to Login/Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Available Requests
        Text("Available Requests", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (availableRequests.isEmpty()) {
            Text("No available requests.")
        } else {
            LazyColumn {
                items(availableRequests) { sos ->
                    VolunteerSosCard(sos, volunteerId, sosViewModel, isAssignment = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My Assignments
        Text("My Assignments", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        if (myAssignments.isEmpty()) {
            Text("You have no assignments.")
        } else {
            LazyColumn {
                items(myAssignments) { sos ->
                    VolunteerSosCard(sos, volunteerId, sosViewModel, isAssignment = true)
                }
            }
        }
    }
}

@Composable
fun VolunteerSosCard(
    sos: SOSRequest,
    volunteerId: String,
    sosViewModel: SosViewModel,
    isAssignment: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Request ID: ${sos.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${sos.status}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                !isAssignment && sos.status != "closed" -> {
                    Button(onClick = { sosViewModel.assignSosToVolunteer(sos.id, volunteerId) }) {
                        Text("Accept Request")
                    }
                }
                isAssignment && sos.status != "closed" -> {
                    Button(onClick = { sosViewModel.closeSosRequest(sos.id) }) {
                        Text("Mark as Completed")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { sosViewModel.assignSosToVolunteer(sos.id, null) }) {
                        Text("Cancel Assignment")
                    }
                }
                sos.status == "closed" -> {
                    Text("This request has been closed.", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
