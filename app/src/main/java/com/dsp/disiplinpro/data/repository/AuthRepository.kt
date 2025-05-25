package com.dsp.disiplinpro.data.repository

import android.util.Log
import com.dsp.disiplinpro.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

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
                fotoProfil = "",
                lastLogin = Date().time
            )
            usersCollection.document(userId).set(user).await()
            Log.d(TAG, "User registered successfully: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${e.message}")
            false
        }
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user

            if (user != null) {
                val userDoc = usersCollection.document(user.uid).get().await()

                if (!userDoc.exists()) {
                    val newUser = User(
                        userId = user.uid,
                        username = user.displayName ?: "",
                        email = user.email ?: "",
                        fotoProfil = user.photoUrl?.toString() ?: "",
                        googleId = user.uid,
                        isGoogleUser = true,
                        lastLogin = Date().time
                    )
                    usersCollection.document(user.uid).set(newUser).await()
                    Log.d(TAG, "Google user created in Firestore: ${user.uid}")
                } else {
                    val userData = hashMapOf<String, Any>(
                        "isGoogleUser" to true,
                        "googleId" to user.uid,
                        "lastLogin" to Date().time
                    )

                    val existingUser = userDoc.toObject(User::class.java)
                    if (existingUser?.fotoProfil.isNullOrEmpty() && !user.photoUrl?.toString().isNullOrEmpty()) {
                        userData["fotoProfil"] = user.photoUrl.toString()
                    }

                    usersCollection.document(user.uid).update(userData).await()
                    Log.d(TAG, "Google user updated in Firestore: ${user.uid}")
                }
            }

            user
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in failed: ${e.message}")
            null
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent to: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send password reset email: ${e.message}")
            false
        }
    }

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null

        return try {
            usersCollection.document(currentUser.uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user: ${e.message}")
            null
        }
    }
}