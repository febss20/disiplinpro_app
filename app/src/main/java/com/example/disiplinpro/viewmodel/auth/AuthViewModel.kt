package com.example.disiplinpro.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val authRepository = AuthRepository()

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Login sukses")
                    onResult(true)
                } else {
                    Log.e(TAG, "Login gagal: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = authRepository.registerUser(username, email, password)
                if (success) {
                    Log.d(TAG, "Registrasi sukses untuk $email")
                    onResult(true)
                } else {
                    Log.e(TAG, "Registrasi gagal")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registrasi error: ${e.message}")
                onResult(false)
            }
        }
    }

    fun logoutUser(onResult: () -> Unit) {
        auth.signOut()
        Log.d(TAG, "Logout berhasil")
        onResult()
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = authRepository.sendPasswordResetEmail(email)
                if (success) {
                    Log.d(TAG, "Email reset password berhasil dikirim ke $email")
                } else {
                    Log.e(TAG, "Gagal mengirim email reset password: email=$email")
                }
                onResult(success)
            } catch (e: Exception) {
                Log.e(TAG, "Password reset error: ${e.message}")
                onResult(false)
            }
        }
    }
}