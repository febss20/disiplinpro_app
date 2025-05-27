package com.dsp.disiplinpro.data.repository.chatbot

import android.content.Context
import android.util.Log
import com.dsp.disiplinpro.data.model.User
import com.dsp.disiplinpro.data.preferences.SecurityPrivacyPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getUserProfile(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSecuritySettings(context: Context?): Map<String, Boolean> {
        if (context == null) return emptyMap()

        val securityPrefs = SecurityPrivacyPreferences(context)

        return try {
            mapOf(
                "biometricLoginEnabled" to securityPrefs.biometricLoginFlow.first(),
                "twoFactorAuthEnabled" to securityPrefs.twoFactorAuthFlow.first(),
                "saveLoginInfoEnabled" to securityPrefs.saveLoginInfoFlow.first(),
                "notificationsEnabled" to securityPrefs.allowNotificationsFlow.first(),
                "shareActivityDataEnabled" to securityPrefs.shareActivityDataFlow.first()
            )
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting security settings: ${e.message}")
            emptyMap()
        }
    }

    suspend fun hasProfilePhoto(): Boolean {
        val user = getUserProfile()
        return !user?.fotoProfil.isNullOrEmpty()
    }

    suspend fun getLoginMethod(): String {
        val user = getUserProfile()
        return if (user?.isGoogleUser == true) "Google" else "Email/Password"
    }

    fun getAppDeveloperInfo(): Map<String, String> {
        return mapOf(
            "developer" to "DisiplinPro dikembangkan oleh satu orang mahasiswa Teknik Informatika Universitas Negeri Surabaya angkatan 2023.",
            "developer_name" to "Alif Rasyid Febriansyah - Ketua Tim & Lead Developer",
            "launch_year" to "2025",
            "email" to "alif.23131@mhs.unesa.ac.id",
            "website" to "corecrab.vercel.app",
            "github" to "https://github.com/febss20",
            "instagram" to "@febss.rasy"
        )
    }
}