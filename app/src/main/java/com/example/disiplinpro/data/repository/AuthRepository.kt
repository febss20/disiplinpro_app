package com.example.disiplinpro.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.example.disiplinpro.data.model.User

private const val TAG = "AuthRepository"

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun registerUser(username: String, email: String, password: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return false

            val user = User(
                userId = userId,
                username = username,
                email = email,
                fotoProfil = ""
            )
            usersCollection.document(userId).set(user).await()
            Log.d(TAG, "User registered successfully: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            val querySnapshot = usersCollection
                .whereEqualTo("email", email)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.w(TAG, "Email not found in database: $email")
                return false
            }

            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email: ${e.message}")
            false
        }
    }
}