package com.example.disasterrelief.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.disasterrelief.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("victim") } // default role
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Spacer(modifier = Modifier.height(8.dp))

        // Role selection
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            listOf("victim", "volunteer", "ngo").forEach { r ->
                RadioButton(selected = role == r, onClick = { role = r })
                Text(text = r.capitalize())
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(onClick = {
            authViewModel.register(name, phone, role, email, password) { success, error ->
                if (success) {
                    if (role == "victim") {
                        navController.navigate("sos_request") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                } else message = error ?: "Unknown error"
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Register")
        }


        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }
    }
}
