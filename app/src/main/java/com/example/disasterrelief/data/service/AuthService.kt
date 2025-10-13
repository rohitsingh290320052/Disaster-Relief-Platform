package com.example.disasterrelief.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        role: String
    ): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: return null

        val userData = mapOf(
            "id" to user.uid,
            "name" to name,
            "role" to role,
            "email" to email
        )
        db.collection("users").document(user.uid).set(userData).await()
        return user
    }

    suspend fun loginUser(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    fun logout() {
        auth.signOut()
    }
}
