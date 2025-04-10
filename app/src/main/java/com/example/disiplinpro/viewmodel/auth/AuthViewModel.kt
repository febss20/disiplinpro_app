package com.example.disiplinpro.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val authRepository = AuthRepository()

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthViewModel", "Login sukses")
                    onResult(true)
                } else {
                    Log.e("AuthViewModel", "Login gagal: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = authRepository.registerUser(username, email, password)
            if (success) {
                Log.d("AuthViewModel", "Registrasi sukses untuk $email")
                onResult(true)
            } else {
                Log.e("AuthViewModel", "Registrasi gagal")
                onResult(false)
            }
        }
    }

    fun logoutUser(onResult: () -> Unit) {
        auth.signOut()
        Log.d("AuthViewModel", "Logout berhasil")
        onResult()
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = authRepository.sendPasswordResetEmail(email)
            if (success) {
                Log.d("AuthViewModel", "Email reset password berhasil dikirim ke $email")
            } else {
                Log.e("AuthViewModel", "Gagal mengirim email reset password: email=$email")
            }
            onResult(success)
        }
    }
}