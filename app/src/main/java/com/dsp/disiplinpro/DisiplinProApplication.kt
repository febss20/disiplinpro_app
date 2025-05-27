package com.dsp.disiplinpro

import android.app.Application
import android.util.Log
import com.dsp.disiplinpro.data.security.NetworkSecurityManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class DisiplinProApplication : Application() {

    companion object {
        private const val TAG = "DisiplinProApplication"
        lateinit var secureHttpClient: OkHttpClient
            private set

        private var isNetworkSecure = false
            private set

        /**
         * Memeriksa apakah implementasi keamanan jaringan sudah bekerja
         * dengan baik (SSL pinning active)
         */
        fun isNetworkSecure(): Boolean {
            return isNetworkSecure
        }
    }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        setupNetworkSecurity()
        setupFirestoreOfflineCache()
        verifySslPinning()

        Log.d(TAG, "DisiplinProApplication initialized")
    }

    private fun setupNetworkSecurity() {
        try {
            val networkSecurityManager = NetworkSecurityManager()
            secureHttpClient = networkSecurityManager.createSecureOkHttpClient()

            Log.d(TAG, "SSL Pinning configured successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up SSL Pinning: ${e.message}")
            secureHttpClient = OkHttpClient.Builder().build()
        }
    }

    private fun verifySslPinning() {
        try {
            val securityManager = NetworkSecurityManager()
            securityManager.verifySslPinning { isSecure ->
                isNetworkSecure = isSecure
                if (isSecure) {
                    Log.d(TAG, "SSL Pinning verification successful!")
                } else {
                    Log.w(TAG, "SSL Pinning verification failed - network might not be secure")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during SSL Pinning verification: ${e.message}")
        }
    }

    private fun setupFirestoreOfflineCache() {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = settings
        Log.d(TAG, "Firestore offline cache configured")
    }
}