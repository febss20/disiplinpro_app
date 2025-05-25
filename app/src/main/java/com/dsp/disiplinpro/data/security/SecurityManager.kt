package com.dsp.disiplinpro.data.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.dsp.disiplinpro.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.BuildConfig

/**
 * Manages security aspects of the application including:
 * - Session management
 * - Security policy enforcement
 * - Device security checks
 */
class SecurityManager(private val context: Context) {
    private lateinit var securityPolicy: AppSecurityPolicy

    init {
        initializeSecurityPolicy()
    }

    private fun initializeSecurityPolicy() {
        securityPolicy = AppSecurityPolicy(context)
        securityPolicy.initialize()

        checkDeviceSecurity()
    }

    private fun checkDeviceSecurity() {
        if (securityPolicy.isDeviceRooted()) {
            Log.w(TAG, "Perangkat terdeteksi dalam kondisi root!")
        }

        if (securityPolicy.isRunningOnEmulator() && !BuildConfig.DEBUG) {
            Log.w(TAG, "Aplikasi berjalan di emulator dalam mode rilis!")
        }

        if (securityPolicy.isBeingDebugged() && !BuildConfig.DEBUG) {
            Log.w(TAG, "Aplikasi sedang di-debug dalam mode rilis!")
        }
    }

    fun checkSessionValidity(activity: Activity) {
        if (FirebaseAuth.getInstance().currentUser != null && !securityPolicy.isSessionValid()) {
            Log.d(TAG, "Sesi telah kedaluwarsa (durasi sesi: 7 hari), mengarahkan ke login")
            Toast.makeText(
                context,
                "Sesi login Anda telah berakhir. Silakan login kembali.",
                Toast.LENGTH_LONG
            ).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
            return
        }

        extendSession()
    }

    fun extendSession() {
        securityPolicy.extendSession()
    }

    companion object {
        private const val TAG = "SecurityManager"
    }
}