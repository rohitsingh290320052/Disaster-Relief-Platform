package com.example.disasterrelief.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun SimpleTextField(value: MutableState<String>, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.value,
        onValueChange = { value.value = it },
        label = { Text(label) },
        modifier = modifier
    )
}
