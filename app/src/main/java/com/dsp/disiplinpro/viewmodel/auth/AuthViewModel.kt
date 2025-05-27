package com.dsp.disiplinpro.viewmodel.auth

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsp.disiplinpro.data.model.User
import com.dsp.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.dsp.disiplinpro.data.repository.AuthRepository
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
import com.dsp.disiplinpro.util.ValidationUtils
import com.dsp.disiplinpro.util.SecureErrorHandler
import com.dsp.disiplinpro.data.security.TwoFactorAuthManager
import com.dsp.disiplinpro.data.security.AppSecurityPolicy
import com.dsp.disiplinpro.data.security.BiometricPromptManager
import com.dsp.disiplinpro.data.preferences.CredentialManager

private const val TAG = "AuthViewModel"

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val authRepository = AuthRepository()

    private val context: Context? = null
    private var securityPreferences: SecurityPrivacyPreferences? = null

    private val _savedEmail = MutableStateFlow<String>("")
    val savedEmail: StateFlow<String> = _savedEmail.asStateFlow()

    val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    var isUserLoggedIn by mutableStateOf(auth.currentUser != null)
        private set

    var isLoading = mutableStateOf(false)

    private val _requires2FA = MutableStateFlow(false)
    val requires2FA: StateFlow<Boolean> = _requires2FA.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private var twoFactorManager: TwoFactorAuthManager? = null
    private var securityPolicy: AppSecurityPolicy? = null
    private var biometricManager: BiometricPromptManager? = null
    private var credentialManager: CredentialManager? = null

    private val _biometricAvailable = MutableStateFlow(false)
    val biometricAvailable: StateFlow<Boolean> = _biometricAvailable.asStateFlow()

    init {
        auth.currentUser?.let {
            refreshCurrentUser()
        }

        auth.addAuthStateListener { firebaseAuth ->
            isUserLoggedIn = firebaseAuth.currentUser != null
        }
    }

    fun initialize(context: Context) {
        securityPreferences = SecurityPrivacyPreferences(context)
        twoFactorManager = TwoFactorAuthManager(context)
        securityPolicy = AppSecurityPolicy(context)
        securityPolicy?.initialize()
        biometricManager = BiometricPromptManager(context)
        credentialManager = CredentialManager(context)

        _biometricAvailable.value = biometricManager?.canAuthenticate() == true
        loadSavedLoginInfo(context)
        checkBiometricSettings(context)
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

    fun loginUser(context: Context, email: String, password: String, onResult: (Boolean, Boolean) -> Unit) {
        if (securityPolicy?.isAccountLocked() == true) {
            _authState.value = AuthState.Error("Terlalu banyak percobaan gagal. Coba lagi nanti.")
            onResult(false, false)
            return
        }

        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.first) {
            _authState.value = AuthState.Error(emailValidation.second ?: "Email tidak valid")
            onResult(false, false)
            return
        }

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.first) {
            _authState.value = AuthState.Error(passwordValidation.second ?: "Password tidak valid")
            onResult(false, false)
            return
        }

        isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading.value = false
                if (task.isSuccessful) {
                    Log.d(TAG, "Login sukses")
                    Log.d(TAG, "Sesi aktif selama 7 hari")
                    securityPolicy?.resetLoginAttempts()
                    saveClearLoginInfo(context, email)
                    _userEmail.value = email

                    val twoFAManager = twoFactorManager ?: TwoFactorAuthManager(context)
                    val requires2FA = twoFAManager.is2FAEnabled()
                    _requires2FA.value = requires2FA

                    if (!requires2FA) {
                        refreshCurrentUser()
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.RequiresTwoFactor
                    }

                    onResult(true, requires2FA)
                } else {
                    val isLocked = securityPolicy?.recordFailedLoginAttempt() ?: false

                    val errorMsg = if (isLocked) {
                        "Terlalu banyak percobaan gagal. Akun dikunci sementara."
                    } else {
                        SecureErrorHandler.handleException(
                            task.exception ?: Exception("Login failed"),
                            TAG
                        )
                    }

                    Log.e(TAG, "Login gagal: $errorMsg")
                    _authState.value = AuthState.Error(errorMsg)
                    onResult(false, false)
                }
            }
    }

    fun registerUser(username: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        val usernameValidation = ValidationUtils.validateUsername(username)
        if (!usernameValidation.first) {
            _authState.value = AuthState.Error(usernameValidation.second ?: "Username tidak valid")
            onResult(false)
            return
        }

        val emailValidation = ValidationUtils.validateEmail(email)
        if (!emailValidation.first) {
            _authState.value = AuthState.Error(emailValidation.second ?: "Email tidak valid")
            onResult(false)
            return
        }

        val passwordValidation = ValidationUtils.validatePassword(password)
        if (!passwordValidation.first) {
            _authState.value = AuthState.Error(passwordValidation.second ?: "Password tidak valid")
            onResult(false)
            return
        }

        val sanitizedUsername = ValidationUtils.sanitizeInput(username)

        viewModelScope.launch {
            isLoading.value = true
            try {
                val success = authRepository.registerUser(sanitizedUsername, email, password)
                if (success) {
                    Log.d(TAG, "Registrasi sukses untuk $email")
                    refreshCurrentUser()
                    _authState.value = AuthState.Authenticated
                    onResult(true)
                } else {
                    Log.e(TAG, "Registrasi gagal")
                    _authState.value = AuthState.Error("Registrasi gagal")
                    onResult(false)
                }
            } catch (e: Exception) {
                val errorMsg = SecureErrorHandler.handleException(e, TAG)
                Log.e(TAG, "Registrasi error: $errorMsg")
                _authState.value = AuthState.Error(errorMsg)
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

    private fun checkBiometricSettings(context: Context) {
        viewModelScope.launch {
            try {
                val prefs = SecurityPrivacyPreferences(context)
                val isBiometricEnabled = prefs.biometricLoginFlow.first()
                val hasCredentials = credentialManager?.hasCredentials() ?: false

                Log.d(TAG, "Biometric login enabled: $isBiometricEnabled, Has credentials: $hasCredentials")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking biometric settings: ${e.message}")
            }
        }
    }

    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val context = activity.applicationContext
        val bioManager = biometricManager ?: BiometricPromptManager(context)
        val credManager = credentialManager ?: CredentialManager(context)

        viewModelScope.launch {
            val prefs = SecurityPrivacyPreferences(context)
            val isBiometricEnabled = prefs.biometricLoginFlow.first()

            if (!isBiometricEnabled) {
                onError("Login sidik jari tidak diaktifkan")
                return@launch
            }

            if (!credManager.hasCredentials()) {
                onError("Tidak ada kredensial tersimpan")
                return@launch
            }

            bioManager.authenticate(
                activity = activity,
                title = "Login dengan Sidik Jari",
                subtitle = "Verifikasi identitas Anda",
                description = "Gunakan sidik jari yang terdaftar untuk masuk ke akun",
                negativeButtonText = "Batal",
                onSuccess = {
                    loginWithSavedCredentials(context) { success, requires2FA ->
                        if (success) {
                            if (!requires2FA) {
                                onSuccess()
                            }
                        } else {
                            onError("Login gagal, silakan coba dengan email dan password")
                        }
                    }
                },
                onError = { _, message ->
                    onError("Error: $message")
                },
                onFailed = {
                    onError("Autentikasi gagal, silakan coba lagi atau gunakan email dan password")
                }
            )
        }
    }

    private fun loginWithSavedCredentials(context: Context, onResult: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val credManager = credentialManager ?: CredentialManager(context)
                if (!credManager.hasCredentials()) {
                    onResult(false, false)
                    return@launch
                }

                val email = credManager.getSavedEmail() ?: ""
                val password = credManager.getSavedPassword() ?: ""

                if (email.isEmpty() || password.isEmpty()) {
                    onResult(false, false)
                    return@launch
                }

                Log.d(TAG, "Login menggunakan kredensial tersimpan")
                loginUser(context, email, password, onResult)
            } catch (e: Exception) {
                Log.e(TAG, "Error selama login dengan kredensial tersimpan: ${e.message}")
                onResult(false, false)
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object RequiresTwoFactor : AuthState()
    data class Error(val message: String) : AuthState()
}