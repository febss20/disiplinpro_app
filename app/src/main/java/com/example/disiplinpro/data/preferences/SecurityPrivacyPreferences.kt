package com.example.disiplinpro.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.securityPrivacyDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_privacy_settings")

class SecurityPrivacyPreferences(private val context: Context) {

    // Preference keys
    companion object {
        val BIOMETRIC_LOGIN = booleanPreferencesKey("biometric_login")
        val TWO_FACTOR_AUTH = booleanPreferencesKey("two_factor_auth")
        val SAVE_LOGIN_INFO = booleanPreferencesKey("save_login_info")
        val SHARE_ACTIVITY_DATA = booleanPreferencesKey("share_activity_data")
        val ALLOW_NOTIFICATIONS = booleanPreferencesKey("allow_notifications")
    }

    // Get individual preference flows
    val biometricLoginFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[BIOMETRIC_LOGIN] ?: false }

    val twoFactorAuthFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[TWO_FACTOR_AUTH] ?: false }

    val saveLoginInfoFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[SAVE_LOGIN_INFO] ?: true }

    val shareActivityDataFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[SHARE_ACTIVITY_DATA] ?: true }

    val allowNotificationsFlow: Flow<Boolean> = context.securityPrivacyDataStore.data
        .map { preferences -> preferences[ALLOW_NOTIFICATIONS] ?: true }

    // Update functions
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

    // Clear all cache and history
    suspend fun clearCacheAndHistory() {
        // This is a placeholder. In a real implementation, you would clear:
        // - App cache files
        // - Any history stored in your app
        // - Anything else that should be cleared according to your app's functionality
    }
}