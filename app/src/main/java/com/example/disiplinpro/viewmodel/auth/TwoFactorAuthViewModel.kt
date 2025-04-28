package com.example.disiplinpro.viewmodel.auth

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.security.TwoFactorAuthManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TwoFactorAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val twoFactorAuthManager = TwoFactorAuthManager(application.applicationContext)

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap: StateFlow<Bitmap?> = _qrCodeBitmap.asStateFlow()

    private val _secret = MutableStateFlow("")
    val secret: StateFlow<String> = _secret.asStateFlow()

    private val _isVerifying = MutableStateFlow(false)
    val isVerifying: StateFlow<Boolean> = _isVerifying.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _setupSuccess = MutableStateFlow(false)
    val setupSuccess: StateFlow<Boolean> = _setupSuccess.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _verificationSuccess = MutableStateFlow(false)
    val verificationSuccess: StateFlow<Boolean> = _verificationSuccess.asStateFlow()

    init {
        viewModelScope.launch {
            _isEnabled.value = twoFactorAuthManager.is2FAEnabled()
        }
    }

    fun generateQRCode() {
        viewModelScope.launch {
            try {
                val user = auth.currentUser ?: run {
                    _errorMessage.value = "Anda harus login untuk mengaktifkan 2FA"
                    return@launch
                }

                val secret = twoFactorAuthManager.generateSecret()
                _secret.value = secret

                val bitmap = twoFactorAuthManager.generateQRCodeBitmap(user)
                _qrCodeBitmap.value = bitmap

                if (bitmap == null) {
                    _errorMessage.value = "Gagal membuat kode QR, silakan coba lagi"
                }
            } catch (e: Exception) {
                Log.e("TwoFactorVM", "Error generating QR code: ${e.message}")
                _errorMessage.value = "Gagal membuat kode 2FA: ${e.message}"
            }
        }
    }

    fun verifyOTP(otp: String) {
        viewModelScope.launch {
            _isVerifying.value = true

            try {
                if (otp.length != 6) {
                    _errorMessage.value = "Kode OTP harus 6 digit"
                    _isVerifying.value = false
                    return@launch
                }

                val isValid = twoFactorAuthManager.verifyOTP(otp)

                if (isValid) {
                    twoFactorAuthManager.completeSetup { success ->
                        _isVerifying.value = false
                        if (success) {
                            _setupSuccess.value = true
                            _isEnabled.value = true
                            Log.d("TwoFactorVM", "2FA setup successful")

                            twoFactorAuthManager.is2FAEnabled()
                        } else {
                            _errorMessage.value = "Gagal menyimpan pengaturan 2FA"
                        }
                    }
                } else {
                    _errorMessage.value = "Kode tidak valid atau sudah kadaluarsa"
                    _isVerifying.value = false
                }
            } catch (e: Exception) {
                Log.e("TwoFactorVM", "Error verifying OTP: ${e.message}")
                _errorMessage.value = "Gagal memverifikasi kode: ${e.message}"
                _isVerifying.value = false
            }
        }
    }

    fun verifyLoginOTP(otp: String) {
        viewModelScope.launch {
            _isVerifying.value = true

            try {
                if (otp.length != 6) {
                    _errorMessage.value = "Kode OTP harus 6 digit"
                    _isVerifying.value = false
                    return@launch
                }

                val isValid = twoFactorAuthManager.verifyOTP(otp)

                if (isValid) {
                    Log.d("TwoFactorVM", "2FA verification successful for login")
                    _verificationSuccess.value = true
                } else {
                    _errorMessage.value = "Kode tidak valid atau sudah kadaluarsa"
                    _isVerifying.value = false
                }
            } catch (e: Exception) {
                Log.e("TwoFactorVM", "Error verifying login OTP: ${e.message}")
                _errorMessage.value = "Gagal memverifikasi kode: ${e.message}"
                _isVerifying.value = false
            }
        }
    }

    fun check2FAConfiguration(): Boolean {
        return twoFactorAuthManager.is2FAEnabled() && twoFactorAuthManager.isSetupComplete()
    }

    fun resetVerificationStatus() {
        _verificationSuccess.value = false
        _isVerifying.value = false
    }

    fun nextStep() {
        if (_currentStep.value < 2) {
            _currentStep.value += 1
        }
    }

    fun previousStep() {
        if (_currentStep.value > 0) {
            _currentStep.value -= 1
        }
    }

    fun disable2FA(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                twoFactorAuthManager.disable2FA { success ->
                    if (success) {
                        _isEnabled.value = false
                        onComplete(true)
                    } else {
                        _errorMessage.value = "Gagal menonaktifkan 2FA"
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("TwoFactorVM", "Error disabling 2FA: ${e.message}")
                _errorMessage.value = "Gagal menonaktifkan 2FA: ${e.message}"
                onComplete(false)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = ""
    }
}