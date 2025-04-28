package com.dsp.disiplinpro.data.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Kelas untuk menerapkan dan mengelola kebijakan keamanan aplikasi
 */
class AppSecurityPolicy(private val context: Context) {

    companion object {
        private const val TAG = "AppSecurityPolicy"

        private const val SESSION_TIMEOUT_MS = 7 * 24 * 60 * 60 * 1000

        private const val MAX_LOGIN_ATTEMPTS = 5

        private const val TEMPORARY_LOCKOUT_DURATION_MS = 5 * 60 * 1000
    }

    private var sessionStartTime = 0L
    private var loginAttempts = 0
    private var lockoutEndTime = 0L

    /**
     * Inisialisasi kebijakan keamanan aplikasi
     */
    fun initialize() {
        Log.d(TAG, "Menginisialisasi kebijakan keamanan aplikasi")
        Log.d(TAG, "Durasi sesi diatur ke ${SESSION_TIMEOUT_MS / (1000 * 60 * 60 * 24)} hari")
        startSession()
    }

    /**
     * Memulai sesi baru
     */
    fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        Log.d(TAG, "Sesi baru dimulai pada: $sessionStartTime")
    }

    /**
     * Memeriksa apakah sesi pengguna masih aktif
     * @return true jika sesi masih valid, false jika sesi telah expired
     */
    fun isSessionValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val sessionAge = currentTime - sessionStartTime

        val isValid = sessionAge < SESSION_TIMEOUT_MS
        if (!isValid) {
            Log.d(TAG, "Sesi telah kedaluwarsa (usia: ${sessionAge}ms)")
        }

        return isValid
    }

    /**
     * Harus dipanggil ketika aktivitas pengguna terdeteksi
     * untuk memperpanjang sesi
     */
    fun extendSession() {
        startSession() // Reset waktu mulai sesi
        Log.d(TAG, "Sesi diperpanjang")
    }

    /**
     * Mencatat upaya login gagal dan memeriksa apakah akun harus dikunci sementara
     * @return true jika akun harus dikunci, false jika tidak
     */
    fun recordFailedLoginAttempt(): Boolean {
        loginAttempts++
        Log.d(TAG, "Upaya login gagal: $loginAttempts")

        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockoutEndTime = System.currentTimeMillis() + TEMPORARY_LOCKOUT_DURATION_MS
            Log.d(TAG, "Akun terkunci sementara hingga: $lockoutEndTime")
            return true
        }

        return false
    }

    /**
     * Memeriksa apakah akun saat ini terkunci
     * @return true jika akun terkunci, false jika tidak
     */
    fun isAccountLocked(): Boolean {
        val currentTime = System.currentTimeMillis()
        val isLocked = currentTime < lockoutEndTime

        if (isLocked) {
            Log.d(TAG, "Akun masih terkunci (sisa: ${lockoutEndTime - currentTime}ms)")
        }

        return isLocked
    }

    /**
     * Mengatur ulang jumlah upaya login setelah login berhasil
     */
    fun resetLoginAttempts() {
        loginAttempts = 0
        lockoutEndTime = 0
        Log.d(TAG, "Upaya login diatur ulang setelah login berhasil")
    }

    /**
     * Memeriksa izin aplikasi yang diperlukan
     * @param permission Izin yang akan diperiksa
     * @return true jika izin diberikan, false jika tidak
     */
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Memeriksa apakah perangkat berada dalam status root
     * @return true jika kemungkinan perangkat di-root, false jika tidak
     */
    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }

    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )

        for (path in paths) {
            if (java.io.File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod3(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Memeriksa apakah aplikasi berjalan di emulator
     * @return true jika kemungkinan berjalan di emulator
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Memeriksa tanda-tanda aplikasi yang di-debug atau ditambahkan tamper
     * @return true jika aplikasi kemungkinan sedang di-debug
     */
    fun isBeingDebugged(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }
}