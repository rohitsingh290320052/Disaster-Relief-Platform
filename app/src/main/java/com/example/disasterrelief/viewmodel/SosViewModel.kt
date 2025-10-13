package com.example.disasterrelief.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disasterrelief.data.model.SOSRequest
import com.example.disasterrelief.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SosViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _sosRequests = MutableStateFlow<List<SOSRequest>>(emptyList())
    val sosRequests: StateFlow<List<SOSRequest>> = _sosRequests

    init {
        listenToSOSRequests()
    }

    /** ✅ CREATE SOS REQUEST **/
    fun createSosRequest(victimId: String, location: GeoPoint, onResult: (Boolean, String?) -> Unit) {
        val sos = SOSRequest(
            victimId = victimId,
            location = location
        )

        db.collection("sos_requests")
            .add(sos)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }


    /** LISTEN TO SOS REQUESTS **/
    private fun listenToSOSRequests() {
        db.collection("sos_requests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(SOSRequest::class.java)?.copy(id = it.id)
                    }
                    _sosRequests.value = list
                }
            }
    }

    fun getVictimSosRequests(victimId: String, onUpdate: (List<SOSRequest>) -> Unit) {
        db.collection("sos_requests")
            .whereEqualTo("victimId", victimId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Optional: Log the error
                    println("❌ Error fetching SOS requests for victim $victimId: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(SOSRequest::class.java)?.copy(id = it.id)
                    }
                    onUpdate(list)
                } else {
                    // Return empty list if no SOS found for victim
                    onUpdate(emptyList())
                }
            }
    }


    fun getVolunteerInfo(volunteerId: String, onResult: (User?) -> Unit) {
        db.collection("users").document(volunteerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)?.copy(id = snapshot.id)
                onResult(user)
            }
            .addOnFailureListener { onResult(null) }
    }

    fun assignSosToVolunteer(sosId: String, volunteerId: String?) {
        viewModelScope.launch {
            db.collection("sos_requests").document(sosId)
                .update(
                    mapOf(
                        "status" to "assigned",
                        "assignedVolunteerId" to volunteerId
                    )
                )
        }
    }

    fun closeSosRequest(sosId: String) {
        viewModelScope.launch {
            db.collection("sos_requests").document(sosId)
                .update("status", "closed")
        }
    }

    fun getVolunteerAssignedSos(volunteerId: String, onUpdate: (List<SOSRequest>) -> Unit) {
        db.collection("sos_requests")
            .whereEqualTo("assignedVolunteerId", volunteerId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(SOSRequest::class.java)?.copy(id = it.id)
                    }
                    onUpdate(list)
                }
            }
    }

    fun cancelSOSRequest(sosId: String, onResult: ((success: Boolean, error: String?) -> Unit)? = null) {
        viewModelScope.launch {
            db.collection("sos_requests").document(sosId)
                .update("status", "cancelled")
                .addOnSuccessListener { onResult?.invoke(true, null) }
                .addOnFailureListener { e -> onResult?.invoke(false, e.message) }
        }
    }

    fun getAllSosRequests(onUpdate: (List<SOSRequest>) -> Unit) {
        db.collection("sos_requests")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull {
                        it.toObject(SOSRequest::class.java)?.copy(id = it.id)
                    }
                    onUpdate(list)
                }
            }
    }


}
