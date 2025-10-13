package com.example.disasterrelief.data.repository

import com.example.disasterrelief.data.model.User
import com.example.disasterrelief.data.model.SOSRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // AUTH
    suspend fun registerWithEmail(email: String, password: String, user: User): Result<Unit> {
        return try {
            val res = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser?.uid ?: res.user?.uid ?: throw Exception("uid null")
            val userWithId = user.copy(id = uid)
            db.collection("users").document(uid).set(userWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // SOS
    suspend fun createSOSRequest(request: SOSRequest): Result<Unit> {
        return try {
            val docRef = db.collection("sos_requests").document()
            val toSave = request.copy(id = docRef.id)
            docRef.set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpenSOSRequests(): Result<List<SOSRequest>> {
        return try {
            val snapshot = db.collection("sos_requests")
                .whereEqualTo("status", "open")
                .get()
                .await()
            val list = snapshot.toObjects(SOSRequest::class.java)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
