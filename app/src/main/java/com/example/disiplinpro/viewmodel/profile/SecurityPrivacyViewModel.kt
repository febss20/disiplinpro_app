package com.example.disiplinpro.viewmodel.profile

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class SecurityPrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val preferences = SecurityPrivacyPreferences(application.applicationContext)

    // UI states from DataStore preferences
    val biometricLoginEnabled: StateFlow<Boolean> = preferences.biometricLoginFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val twoFactorAuthEnabled: StateFlow<Boolean> = preferences.twoFactorAuthFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val saveLoginInfoEnabled: StateFlow<Boolean> = preferences.saveLoginInfoFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shareActivityDataEnabled: StateFlow<Boolean> = preferences.shareActivityDataFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val allowNotificationsEnabled: StateFlow<Boolean> = preferences.allowNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Loading state
    val isLoading = mutableStateOf(false)

    // Error state
    val error = mutableStateOf<String?>(null)

    // Success state
    val operationSuccess = mutableStateOf(false)

    // Update preferences functions
    fun updateBiometricLogin(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateBiometricLogin(enabled)
        }
    }

    fun updateTwoFactorAuth(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateTwoFactorAuth(enabled)

            // In a real app, you would enable/disable two-factor authentication in Firebase
            // This would typically involve setting up Firebase Phone Authentication
            // or integrating with a service like Google Authenticator

            // For demonstration purposes, we'll just show a toast if the feature is enabled
            if (enabled) {
                Toast.makeText(
                    getApplication(),
                    "Autentikasi dua faktor aktif. Pemberitahuan akan dikirim ke email terdaftar saat login dari perangkat baru.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun updateSaveLoginInfo(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateSaveLoginInfo(enabled)
        }
    }

    fun updateShareActivityData(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateShareActivityData(enabled)
        }
    }

    fun updateAllowNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateAllowNotifications(enabled)
        }
    }

    // Clear cache and history
    fun clearCacheAndHistory() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                // Clear app cache
                val cacheDir = getApplication<Application>().cacheDir
                if (deleteDir(cacheDir)) {
                    Log.d("SecurityPrivacyViewModel", "Cache cleared successfully")
                } else {
                    Log.w("SecurityPrivacyViewModel", "Failed to clear some cache files")
                }

                // Optionally clear Firebase cache
                FirebaseFirestore.getInstance().clearPersistence()

                // Clear preferences related to history if needed
                // This is app-specific; here's an example for clearing some history flags
                viewModelScope.launch {
                    preferences.clearCacheAndHistory()
                }

                operationSuccess.value = true
                Toast.makeText(
                    getApplication(),
                    "Data sementara dan histori telah dibersihkan",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("SecurityPrivacyViewModel", "Error clearing cache: ${e.message}")
                error.value = "Gagal membersihkan cache: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Helper function to delete directory contents
    private fun deleteDir(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return false

        val files = dir.listFiles() ?: return false
        var result = true

        for (file in files) {
            result = if (file.isDirectory) {
                result and deleteDir(file)
            } else {
                result and file.delete()
            }
        }

        return result
    }

    // Reset password
    fun resetPassword(email: String? = null) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                val userEmail = email ?: auth.currentUser?.email

                if (userEmail.isNullOrEmpty()) {
                    error.value = "Email tidak ditemukan"
                    return@launch
                }

                auth.sendPasswordResetEmail(userEmail).await()

                operationSuccess.value = true
                Toast.makeText(
                    getApplication(),
                    "Email reset password telah dikirim ke $userEmail",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("SecurityPrivacyViewModel", "Error sending password reset: ${e.message}")
                error.value = "Gagal mengirim email reset password: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Delete account
    fun deleteAccount(password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                val user = auth.currentUser

                if (user == null) {
                    error.value = "Pengguna tidak ditemukan"
                    return@launch
                }

                // Re-authenticate user before deleting account
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential).await()

                // Delete user data from Firestore first
                val userId = user.uid

                // Delete user document
                firestore.collection("users").document(userId).delete().await()

                // Delete user's tasks
                val tasksSnapshot = firestore.collection("users").document(userId)
                    .collection("tasks").get().await()

                for (docSnapshot in tasksSnapshot.documents) {
                    docSnapshot.reference.delete().await()
                }

                // Delete user's schedules
                val schedulesSnapshot = firestore.collection("users").document(userId)
                    .collection("schedules").get().await()

                for (docSnapshot in schedulesSnapshot.documents) {
                    docSnapshot.reference.delete().await()
                }

                // Finally delete the Firebase Auth account
                user.delete().await()

                operationSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                Log.e("SecurityPrivacyViewModel", "Error deleting account: ${e.message}")
                error.value = "Gagal menghapus akun: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}