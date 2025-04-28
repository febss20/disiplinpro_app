package com.dsp.disiplinpro.viewmodel.profile

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.dsp.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.dsp.disiplinpro.data.preferences.CredentialManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.asStateFlow
import com.dsp.disiplinpro.data.security.TwoFactorAuthManager

class SecurityPrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val preferences = SecurityPrivacyPreferences(application.applicationContext)
    private val credentialManager = CredentialManager(application.applicationContext)
    private val twoFactorManager = TwoFactorAuthManager(application.applicationContext)

    val biometricLoginEnabled: StateFlow<Boolean> = preferences.biometricLoginFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _twoFactorEnabled = MutableStateFlow(false)
    val twoFactorAuthEnabled: StateFlow<Boolean> = _twoFactorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val saveLoginInfoFlow: StateFlow<Boolean> = preferences.saveLoginInfoFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shareActivityDataEnabled: StateFlow<Boolean> = preferences.shareActivityDataFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val allowNotificationsEnabled: StateFlow<Boolean> = preferences.allowNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isLoading = mutableStateOf(false)

    val error = mutableStateOf<String?>(null)

    val operationSuccess = mutableStateOf(false)

    private val _savedCredentials = MutableStateFlow<Pair<String, String>>("" to "")
    val savedCredentials = _savedCredentials.asStateFlow()

    private val _hasCredentials = MutableStateFlow(false)
    val hasCredentials = _hasCredentials.asStateFlow()

    init {
        loadSavedCredentials()
        refreshTwoFactorStatus()
    }

    /**
     * Memuat kredensial tersimpan (fungsi publik)
     */
    fun loadSavedCredentials() {
        viewModelScope.launch {
            val saveLoginEnabled = preferences.saveLoginInfoFlow.first()
            if (saveLoginEnabled && credentialManager.hasCredentials()) {
                val email = credentialManager.getSavedEmail() ?: ""
                val password = credentialManager.getSavedPassword() ?: ""
                _savedCredentials.value = email to password
                _hasCredentials.value = true
                Log.d("SecurityPrivacyViewModel", "Kredensial tersimpan dimuat")
            } else {
                _hasCredentials.value = false
                Log.d("SecurityPrivacyViewModel", "Tidak ada kredensial tersimpan")
            }
        }
    }

    /**
     * Menyimpan kredensial
     */
    fun saveCredentials(email: String, password: String) {
        viewModelScope.launch {
            val success = credentialManager.saveCredentials(email, password)
            if (success) {
                _savedCredentials.value = email to password
                _hasCredentials.value = true

                if (!preferences.saveLoginInfoFlow.first()) {
                    preferences.updateSaveLoginInfo(true)
                }

                Toast.makeText(
                    getApplication(),
                    "Informasi login berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    getApplication(),
                    "Gagal menyimpan informasi login",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Menghapus kredensial tersimpan
     */
    fun deleteCredentials() {
        viewModelScope.launch {
            credentialManager.clearCredentials()
            _savedCredentials.value = "" to ""
            _hasCredentials.value = false

            Toast.makeText(
                getApplication(),
                "Informasi login berhasil dihapus",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun updateBiometricLogin(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateBiometricLogin(enabled)
        }
    }

    fun updateTwoFactorAuth(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateTwoFactorAuth(enabled)
            _twoFactorEnabled.value = enabled

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
            if (!enabled) {
                deleteCredentials()

                val appContext = getApplication<Application>().applicationContext
                val sharedPrefs = appContext.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()

                Log.d("SecurityPrivacyViewModel", "Informasi login yang tersimpan telah dihapus")
                Toast.makeText(
                    appContext,
                    "Informasi login yang tersimpan telah dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loadSavedCredentials()
            }
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
            if (!enabled) {
                val appContext = getApplication<Application>().applicationContext

                WorkManager.getInstance(appContext).cancelAllWorkByTag("notification_tag")
                WorkManager.getInstance(appContext).cancelAllWorkByTag("overdue_notification_tag")

                Log.d("SecurityPrivacyViewModel", "Semua notifikasi terjadwal dibatalkan")
                Toast.makeText(
                    appContext,
                    "Semua notifikasi telah dinonaktifkan",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun clearCacheAndHistory() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                val cacheDir = getApplication<Application>().cacheDir
                if (deleteDir(cacheDir)) {
                    Log.d("SecurityPrivacyViewModel", "Cache cleared successfully")
                } else {
                    Log.w("SecurityPrivacyViewModel", "Failed to clear some cache files")
                }

                FirebaseFirestore.getInstance().clearPersistence()

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

                val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential).await()

                val userId = user.uid

                firestore.collection("users").document(userId).delete().await()

                val tasksSnapshot = firestore.collection("users").document(userId)
                    .collection("tasks").get().await()

                for (docSnapshot in tasksSnapshot.documents) {
                    docSnapshot.reference.delete().await()
                }

                val schedulesSnapshot = firestore.collection("users").document(userId)
                    .collection("schedules").get().await()

                for (docSnapshot in schedulesSnapshot.documents) {
                    docSnapshot.reference.delete().await()
                }

                if (isGoogleUser) {
                    try {
                        val context = getApplication<Application>().applicationContext
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)

                        googleSignInClient.revokeAccess().await()
                        googleSignInClient.signOut().await()
                        Log.d("SecurityPrivacyViewModel", "Google access revoked for user $userId")
                    } catch (e: Exception) {
                        Log.e("SecurityPrivacyViewModel", "Failed to revoke Google access: ${e.message}")
                    }
                }

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

    fun refreshTwoFactorStatus() {
        viewModelScope.launch {
            val is2FAEnabled = twoFactorManager.is2FAEnabled()
            _twoFactorEnabled.value = is2FAEnabled
            preferences.updateTwoFactorAuth(is2FAEnabled)
            Log.d("SecurityPrivacyViewModel", "2FA status refreshed: $is2FAEnabled")
        }
    }
}