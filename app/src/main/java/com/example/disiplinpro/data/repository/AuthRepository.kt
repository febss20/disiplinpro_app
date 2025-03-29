package com.example.disiplinpro.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.example.disiplinpro.data.model.User

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(username: String, email: String, password: String): Boolean {
        return try {
            // Buat akun dengan email dan password di Firebase Authentication
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return false

            // Simpan data pengguna ke Firestore
            val user = User(
                userId = userId,
                username = username,
                email = email,
                fotoProfil = "" // Default kosong, bisa diisi nanti
            )
            firestore.collection("users").document(userId).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean {
        return try {
            // Periksa apakah email ada di Firestore
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                // Email tidak terdaftar
                return false
            }

            // Jika email terdaftar, kirim email reset
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}