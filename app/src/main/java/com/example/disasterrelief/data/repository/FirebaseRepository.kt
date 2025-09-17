package com.example.disasterrelief.data.repository

import com.example.disasterrelief.data.model.User
import com.example.disasterrelief.data.model.SOSRequest
import com.example.disasterrelief.data.model.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // 🔹 Register a new user
    suspend fun registerUser(user: User): Boolean {
        return try {
            db.collection("users")
                .document(user.id)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 Get current user
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // 🔹 Save SOS request
    suspend fun createSOSRequest(request: SOSRequest): Boolean {
        return try {
            db.collection("sos_requests")
                .document(request.id)
                .set(request)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 Fetch all open SOS requests
    suspend fun getOpenSOSRequests(): List<SOSRequest> {
        return try {
            db.collection("sos_requests")
                .whereEqualTo("status", "open")
                .get()
                .await()
                .toObjects(SOSRequest::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 Add NGO Resource
    suspend fun addResource(resource: Resource): Boolean {
        return try {
            db.collection("resources")
                .document(resource.id)
                .set(resource)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 Fetch all resources
    suspend fun getResources(): List<Resource> {
        return try {
            db.collection("resources")
                .get()
                .await()
                .toObjects(Resource::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
