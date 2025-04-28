package com.dsp.disiplinpro.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

val Context.securityPrivacyDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_privacy_settings")

class SecurityPrivacyPreferences(private val context: Context) {

    companion object {
        val BIOMETRIC_LOGIN = booleanPreferencesKey("biometric_login")
        val TWO_FACTOR_AUTH = booleanPreferencesKey("two_factor_auth")
        val SAVE_LOGIN_INFO = booleanPreferencesKey("save_login_info")
        val SHARE_ACTIVITY_DATA = booleanPreferencesKey("share_activity_data")
        val ALLOW_NOTIFICATIONS = booleanPreferencesKey("allow_notifications")
    }

    val biometricLoginFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[BIOMETRIC_LOGIN] ?: false }

    val twoFactorAuthFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[TWO_FACTOR_AUTH] ?: false }

    val saveLoginInfoFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[SAVE_LOGIN_INFO] ?: false }

    val shareActivityDataFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[SHARE_ACTIVITY_DATA] ?: true }

    val allowNotificationsFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[ALLOW_NOTIFICATIONS] ?: true }

    suspend fun updateBiometricLogin(enabled: Boolean) {
        context.securityPrivacyDataStore.edit { preferences ->
            preferences[BIOMETRIC_LOGIN] = enabled
        }
    }

    suspend fun updateTwoFactorAuth(enabled: Boolean) {
        context.securityPrivacyDataStore.edit { preferences ->
            preferences[TWO_FACTOR_AUTH] = enabled
        }
    }

    suspend fun updateSaveLoginInfo(enabled: Boolean) {
        context.securityPrivacyDataStore.edit { preferences ->
            preferences[SAVE_LOGIN_INFO] = enabled
        }
    }

    suspend fun updateShareActivityData(enabled: Boolean) {
        context.securityPrivacyDataStore.edit { preferences ->
            preferences[SHARE_ACTIVITY_DATA] = enabled
        }
    }

    suspend fun updateAllowNotifications(enabled: Boolean) {
        context.securityPrivacyDataStore.edit { preferences ->
            preferences[ALLOW_NOTIFICATIONS] = enabled
        }
    }

    suspend fun clearCacheAndHistory(context: Context) {
        try {
            val securitySettings = context.securityPrivacyDataStore.data.first()

            val firestore = FirebaseFirestore.getInstance()
            firestore.clearPersistence().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SecurityPrivacy", "Firestore cache berhasil dihapus")
                } else {
                    Log.e("SecurityPrivacy", "Error menghapus cache Firestore: ${task.exception?.message}")
                }
            }

            try {
                val cacheDir = context.cacheDir
                deleteDir(cacheDir)
                Log.d("SecurityPrivacy", "Cache dir berhasil dihapus")
            } catch (e: Exception) {
                Log.e("SecurityPrivacy", "Gagal menghapus cache dir: ${e.message}")
            }

            try {
                context.externalCacheDir?.let {
                    deleteDir(it)
                    Log.d("SecurityPrivacy", "External cache dir berhasil dihapus")
                }
            } catch (e: Exception) {
                Log.e("SecurityPrivacy", "Gagal menghapus external cache: ${e.message}")
            }

            try {
                context.deleteDatabase("webview.db")
                context.deleteDatabase("webviewCache.db")
                Log.d("SecurityPrivacy", "WebView cache berhasil dihapus")
            } catch (e: Exception) {
                Log.e("SecurityPrivacy", "Gagal menghapus WebView cache: ${e.message}")
            }

            context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()

            Log.d("SecurityPrivacy", "Semua cache dan riwayat berhasil dihapus")
        } catch (e: Exception) {
            Log.e("SecurityPrivacy", "Error dalam menghapus cache dan riwayat: ${e.message}")
            throw e
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val children = dir.list() ?: return false
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }

        return dir.delete()
    }
}