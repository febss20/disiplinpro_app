package com.example.disiplinpro.viewmodel.auth

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.model.User
import com.example.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.example.disiplinpro.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    private val context: Context? = null
    private var securityPreferences: SecurityPrivacyPreferences? = null

    private val _savedEmail = MutableStateFlow<String>("")
    val savedEmail: StateFlow<String> = _savedEmail.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val isLoading = mutableStateOf(false)

    init {
        auth.currentUser?.let {
            refreshCurrentUser()
        }
    }

    fun initialize(context: Context) {
        securityPreferences = SecurityPrivacyPreferences(context)
        loadSavedLoginInfo(context)
    }

    private fun loadSavedLoginInfo(context: Context) {
        viewModelScope.launch {
            try {
                val prefs = SecurityPrivacyPreferences(context)
                val saveLoginEnabled = prefs.saveLoginInfoFlow.first()

                if (saveLoginEnabled) {
                    val sharedPrefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                    val email = sharedPrefs.getString("saved_email", "") ?: ""

                    if (email.isNotEmpty()) {
                        _savedEmail.value = email
                        Log.d(TAG, "Email tersimpan dimuat: $email")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved login info: ${e.message}")
            }
        }
    }

    private fun saveClearLoginInfo(context: Context, email: String) {
        viewModelScope.launch {
            try {
                val prefs = SecurityPrivacyPreferences(context)
                val saveLoginEnabled = prefs.saveLoginInfoFlow.first()

                val sharedPrefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()

                if (saveLoginEnabled) {
                    editor.putString("saved_email", email)
                    _savedEmail.value = email
                    Log.d(TAG, "Email disimpan: $email")
                } else {
                    editor.clear()
                    _savedEmail.value = ""
                    Log.d(TAG, "Info login dihapus")
                }

                editor.apply()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving/clearing login info: ${e.message}")
            }
        }
    }

    private fun refreshCurrentUser() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val user = authRepository.getCurrentUser()
                _currentUser.value = user
                _authState.value = if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing user: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun loginUser(context: Context, email: String, password: String, onResult: (Boolean) -> Unit) {
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    Log.d(TAG, "Login sukses")
                    saveClearLoginInfo(context, email)
                    refreshCurrentUser()
                    _authState.value = AuthState.Authenticated
                    onResult(true)
                } else {
                    Log.e(TAG, "Login gagal: ${task.exception?.message}")
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                    onResult(false)
                }
            }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = authRepository.registerUser(username, email, password)
                if (success) {
                    Log.d(TAG, "Registrasi sukses untuk $email")
                    refreshCurrentUser()
                    _authState.value = AuthState.Authenticated
                    onResult(true)
                } else {
                    Log.e(TAG, "Registrasi gagal")
                    _authState.value = AuthState.Error("Registration failed")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registrasi error: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
                onResult(false)
            } finally {
                isLoading.value = false
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        try {
            isLoading.value = true
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            account?.let {
                firebaseAuthWithGoogle(it)
            } ?: run {
                isLoading.value = false
                _authState.value = AuthState.Error("Google sign in failed")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google sign in failed", e)
            isLoading.value = false
            _authState.value = AuthState.Error(e.message ?: "Google sign in failed")
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val idToken = account.idToken ?: throw Exception("ID Token is null")
                val user = authRepository.signInWithGoogle(idToken)

                if (user != null) {
                    _currentUser.value = authRepository.getCurrentUser()
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Google authentication failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase auth with Google failed: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            } finally {
                isLoading.value = false
            }
        }
    }

    fun logoutUser(context: Context, onResult: () -> Unit) {
        isLoading.value = true

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)

            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    Log.d(TAG, "Google access revoked successfully")
                } else {
                    Log.w(TAG, "Failed to revoke Google access: ${revokeTask.exception?.message}")
                }

                googleSignInClient.signOut().addOnCompleteListener { signOutTask ->
                    if (signOutTask.isSuccessful) {
                        Log.d(TAG, "Google sign out successful")
                    } else {
                        Log.w(TAG, "Failed to sign out from Google: ${signOutTask.exception?.message}")
                    }

                    auth.signOut()
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated

                    Log.d(TAG, "Logout berhasil")
                    isLoading.value = false
                    onResult()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
            auth.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            isLoading.value = false
            onResult()
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
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
            } finally {
                isLoading.value = false
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}