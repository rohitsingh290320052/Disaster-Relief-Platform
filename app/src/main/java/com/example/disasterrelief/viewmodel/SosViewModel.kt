package com.example.disasterrelief.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disasterrelief.data.model.SOSRequest
import com.example.disasterrelief.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class SosViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _sosRequests = MutableStateFlow<List<SOSRequest>>(emptyList())
    val sosRequests: StateFlow<List<SOSRequest>> = _sosRequests

    // 🔹 Create new SOS
    fun createSOS(userId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val request = SOSRequest(
                id = UUID.randomUUID().toString(),
                victimId = userId,
                location = com.google.firebase.firestore.GeoPoint(lat, lng),
                status = "open"
            )
            repository.createSOSRequest(request)
            loadOpenSOSRequests()
        }
    }

    // 🔹 Load all open SOS requests
    fun loadOpenSOSRequests() {
        viewModelScope.launch {
            _sosRequests.value = repository.getOpenSOSRequests()
        }
    }
}
