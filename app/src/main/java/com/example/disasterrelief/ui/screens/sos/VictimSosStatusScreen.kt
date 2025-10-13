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
fun VictimSosStatusScreen(
    navController: NavController,
    sosViewModel: SosViewModel = viewModel()
) {
    val victimId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var sosList by remember { mutableStateOf<List<SOSRequest>>(emptyList()) }

    // Listen to victim SOS requests
    LaunchedEffect(victimId) {
        sosViewModel.getVictimSosRequests(victimId) { list ->
            sosList = list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My SOS Requests",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f) // take remaining space
        ) {
            items(sosList) { sos ->
                SosStatusCard(sos, navController = navController, sosViewModel = sosViewModel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Back to Register/Login button
        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("register") {
                    popUpTo(0) // clears backstack
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Text("🔙 Back to Register/Login")
        }
    }
}


@Composable
fun SosStatusCard(
    sos: SOSRequest,
    navController: NavController,
    sosViewModel: SosViewModel = viewModel()
) {
    var volunteerName by remember { mutableStateOf<String?>(null) }
    var volunteerPhone by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sos.assignedVolunteerId) {
        if (sos.assignedVolunteerId != null) {
            sosViewModel.getVolunteerInfo(sos.assignedVolunteerId!!) { user ->
                volunteerName = user?.name
                volunteerPhone = user?.phone
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Request ID: ${sos.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${sos.status}", style = MaterialTheme.typography.bodyMedium)

            if (volunteerName != null) {
                Text(
                    text = "Volunteer: $volunteerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (volunteerPhone != null) {
                    Text(
                        text = "Phone: $volunteerPhone",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (sos.status) {
                "open" -> {
                    // Cancel and send new SOS
                    Button(
                        onClick = {
                            sosViewModel.cancelSOSRequest(sos.id) { success, _ ->
                                if (success) navController.navigate("sos_request")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🚨 Cancel SOS & Send New")
                    }
                }

                "assigned" -> {
                    Text(
                        text = "Volunteer is on the way!",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                "closed" -> {
                    Text(
                        text = "This SOS request has been closed.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    // No button here; user must go to SOS screen manually
                }
            }
        }
    }
}
