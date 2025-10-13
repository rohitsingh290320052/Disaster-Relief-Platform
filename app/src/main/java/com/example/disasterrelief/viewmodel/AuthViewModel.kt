package com.example.disasterrelief.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.logging.Handler

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun register(
        name: String,
        phone: String,
        role: String,
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "phone" to phone,
                    "email" to email,
                    "role" to role
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Log.d("AuthViewModel", "✅ User registered with role: $role")
                        callback(true, null)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthViewModel", "❌ Firestore error: ${e.message}")
                        callback(false, e.message)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "❌ Auth error: ${e.message}")
                callback(false, e.message)
            }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "✅ Login successful")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "❌ Login failed: ${e.message}")
                callback(false, e.message)
            }
    }

    fun getUserRole(callback: (String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            callback(null)
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role")
                Log.d("AuthViewModel", "✅ Fetched role: $role for user $uid")
                callback(role)
            }
            .addOnFailureListener { e ->
                Log.e("AuthViewModel", "❌ Failed to fetch role: ${e.message}")
                callback(null)
            }
    }

    fun currentUserId(): String? = auth.currentUser?.uid

    fun logout() {
        auth.signOut()
    }
}
