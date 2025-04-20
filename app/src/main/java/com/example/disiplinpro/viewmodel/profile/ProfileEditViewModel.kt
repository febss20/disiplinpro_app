package com.example.disiplinpro.viewmodel.profile

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.disiplinpro.data.model.User
import com.example.disiplinpro.data.repository.S3Repository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URL
import java.util.Date
import java.util.concurrent.TimeUnit

class ProfileEditViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val s3Repository = S3Repository(application.applicationContext)

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    init {
        loadCurrentUserData()
    }

    private fun loadCurrentUserData() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userDoc = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    val userData = userDoc.toObject(User::class.java)
                    if (userData != null) {
                        val photoUrl = userData.fotoProfil
                        val photoObjectKey = userData.fotoProfilObjectKey
                        val photoExpiration = userData.fotoProfilExpiration ?: 0L

                        val finalPhotoUrl = if (!photoUrl.isNullOrEmpty() && !photoObjectKey.isNullOrEmpty() &&
                            photoExpiration > 0 && Date().time > photoExpiration - TimeUnit.DAYS.toMillis(1)) {
                            try {
                                val newUrl = s3Repository.refreshProfilePhotoUrl(photoObjectKey)
                                val newExpiration = Date().time + TimeUnit.DAYS.toMillis(7)

                                val photoUpdate = mapOf(
                                    "fotoProfil" to newUrl,
                                    "fotoProfilExpiration" to newExpiration
                                )
                                userDoc.reference.update(photoUpdate).await()
                                newUrl
                            } catch (e: Exception) {
                                photoUrl
                            }
                        } else {
                            photoUrl
                        }

                        _uiState.value = _uiState.value.copy(
                            username = userData.username ?: "",
                            email = currentUser.email ?: "",
                            profilePhotoUrl = finalPhotoUrl,
                            profilePhotoObjectKey = photoObjectKey,
                            profilePhotoExpiration = photoExpiration,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memuat data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun setUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun setEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun setCurrentPassword(password: String) {
        _uiState.value = _uiState.value.copy(currentPassword = password)
    }

    fun setNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password)
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            showCurrentPassword = !_uiState.value.showCurrentPassword
        )
    }

    fun toggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            showNewPassword = !_uiState.value.showNewPassword
        )
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun validateInputs(): Boolean {
        var isValid = true
        var errorMessage: String? = null

        if (_uiState.value.username.isBlank()) {
            errorMessage = "Username tidak boleh kosong"
            isValid = false
        }

        else if (_uiState.value.email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            errorMessage = "Email tidak valid"
            isValid = false
        }

        else if (_uiState.value.newPassword.isNotEmpty()) {
            if (_uiState.value.currentPassword.isEmpty()) {
                errorMessage = "Password saat ini diperlukan untuk mengubah password"
                isValid = false
            } else if (_uiState.value.newPassword.length < 6) {
                errorMessage = "Password baru minimal 6 karakter"
                isValid = false
            }
        }

        _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
        return isValid
    }

    fun updateProfile() {
        if (!validateInputs()) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, isSuccess = false)

        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser ?: throw Exception("User tidak ditemukan")
                val userId = currentUser.uid

                val photoData = if (_selectedImageUri.value != null) {
                    if (!_uiState.value.profilePhotoUrl.isNullOrEmpty() &&
                        !_uiState.value.profilePhotoObjectKey.isNullOrEmpty()) {
                        s3Repository.deleteProfilePhoto(_uiState.value.profilePhotoUrl!!)
                    }

                    val presignedUrl = s3Repository.uploadProfilePhoto(_selectedImageUri.value!!, userId)

                    val url = URL(presignedUrl)
                    val objectKey = url.path.removePrefix("/").substringBefore("?")

                    val expirationTime = Date().time + TimeUnit.DAYS.toMillis(7)

                    mapOf(
                        "fotoProfil" to presignedUrl,
                        "fotoProfilObjectKey" to objectKey,
                        "fotoProfilExpiration" to expirationTime
                    )
                } else {
                    mapOf(
                        "fotoProfil" to (_uiState.value.profilePhotoUrl ?: "")
                    )
                }

                if (currentUser.email != _uiState.value.email) {
                    if (_uiState.value.currentPassword.isNotEmpty()) {
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, _uiState.value.currentPassword)
                        currentUser.reauthenticate(credential).await()
                        currentUser.updateEmail(_uiState.value.email).await()
                    } else {
                        throw Exception("Password saat ini diperlukan untuk mengubah email")
                    }
                }

                if (_uiState.value.newPassword.isNotEmpty() && _uiState.value.currentPassword.isNotEmpty()) {
                    if (currentUser.email != _uiState.value.email) {
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, _uiState.value.currentPassword)
                        currentUser.reauthenticate(credential).await()
                    }
                    currentUser.updatePassword(_uiState.value.newPassword).await()
                }

                val userData = hashMapOf<String, Any>(
                    "username" to _uiState.value.username
                )

                userData.putAll(photoData)

                firestore.collection("users").document(userId).update(userData).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentPassword = "",
                    newPassword = "",
                    profilePhotoUrl = photoData["fotoProfil"] as? String ?: _uiState.value.profilePhotoUrl,
                    profilePhotoObjectKey = photoData["fotoProfilObjectKey"] as? String ?: _uiState.value.profilePhotoObjectKey,
                    profilePhotoExpiration = photoData["fotoProfilExpiration"] as? Long ?: _uiState.value.profilePhotoExpiration
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal menyimpan perubahan: ${e.message}"
                )
            }
        }
    }
}

data class ProfileEditUiState(
    val username: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val showCurrentPassword: Boolean = false,
    val showNewPassword: Boolean = false,
    val profilePhotoUrl: String? = null,
    val profilePhotoObjectKey: String? = null,
    val profilePhotoExpiration: Long? = null,
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)