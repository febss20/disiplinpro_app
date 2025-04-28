package com.dsp.disiplinpro.data.security

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.apache.commons.codec.binary.Base32
import java.net.URLEncoder
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

/**
 * Manager untuk autentikasi dua faktor menggunakan TOTP (Time-based One-Time Password)
 * Kompatibel dengan aplikasi autentikator seperti Google Authenticator, Authy, dll.
 */
class TwoFactorAuthManager(private val context: Context) {

    companion object {
        private const val TAG = "TwoFactorAuthManager"
        private const val PREFS_NAME = "two_factor_auth_prefs"
        private const val SECRET_KEY = "2fa_secret"
        private const val ENABLED_KEY = "2fa_enabled"
        private const val SETUP_COMPLETE_KEY = "2fa_setup_complete"

        private const val QR_CODE_SIZE = 512
        private const val OTP_DIGITS = 6
        private const val TIME_STEP = 30
        private const val SECRET_SIZE = 20
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Memeriksa apakah 2FA diaktifkan untuk pengguna
     */
    fun is2FAEnabled(): Boolean {
        return prefs.getBoolean(ENABLED_KEY, false)
    }

    /**
     * Memeriksa apakah setup 2FA telah selesai
     */
    fun isSetupComplete(): Boolean {
        return prefs.getBoolean(SETUP_COMPLETE_KEY, false)
    }

    /**
     * Menghasilkan kunci rahasia baru untuk 2FA
     */
    fun generateSecret(): String {
        val random = SecureRandom()
        val bytes = ByteArray(SECRET_SIZE)
        random.nextBytes(bytes)

        val base32 = Base32()
        val encodedKey = base32.encodeAsString(bytes).replace("=", "").chunked(4).joinToString(" ")

        prefs.edit().putString(SECRET_KEY, encodedKey.replace(" ", "")).apply()

        return encodedKey
    }

    /**
     * Mendapatkan kunci rahasia yang sudah disimpan
     */
    private fun getSecret(): String? {
        val secret = prefs.getString(SECRET_KEY, null)
        Log.d(TAG, "Retrieved secret: ${secret?.take(4)}... (exists: ${secret != null})")
        return secret
    }

    /**
     * Memeriksa apakah kunci rahasia sudah ada
     */
    fun hasSecret(): Boolean {
        val hasSecret = prefs.getString(SECRET_KEY, null) != null
        Log.d(TAG, "Checking if secret exists: $hasSecret")
        return hasSecret
    }

    /**
     * Menyimpan status 2FA ke Firestore
     */
    fun save2FAStatusToFirestore(enabled: Boolean, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: run {
            Log.e(TAG, "Tidak ada pengguna yang login")
            onComplete(false)
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .update("twoFactorAuthEnabled", enabled)
            .addOnSuccessListener {
                Log.d(TAG, "Status 2FA berhasil disimpan: $enabled")
                prefs.edit().putBoolean(ENABLED_KEY, enabled).apply()
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Gagal menyimpan status 2FA: ${e.message}")
                onComplete(false)
            }
    }

    /**
     * Menyelesaikan setup 2FA
     */
    fun completeSetup(onComplete: (Boolean) -> Unit) {
        prefs.edit().putBoolean(SETUP_COMPLETE_KEY, true).apply()
        save2FAStatusToFirestore(true, onComplete)
    }

    /**
     * Menonaktifkan 2FA
     */
    fun disable2FA(onComplete: (Boolean) -> Unit) {
        prefs.edit()
            .putBoolean(ENABLED_KEY, false)
            .putBoolean(SETUP_COMPLETE_KEY, false)
            .apply()

        save2FAStatusToFirestore(false, onComplete)
    }

    /**
     * Menghasilkan URL untuk QR code
     */
    fun generateQRCodeURL(user: FirebaseUser): String {
        val secret = getSecret() ?: generateSecret().replace(" ", "")
        val issuer = "DisiplinPro"
        val email = URLEncoder.encode(user.email ?: "user", "UTF-8")

        return "otpauth://totp/$issuer:$email?secret=$secret&issuer=$issuer&algorithm=SHA1&digits=$OTP_DIGITS&period=$TIME_STEP"
    }

    /**
     * Menghasilkan QR code sebagai Bitmap
     */
    fun generateQRCodeBitmap(user: FirebaseUser): Bitmap? {
        return try {
            val url = generateQRCodeURL(user)
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghasilkan QR code: ${e.message}")
            null
        }
    }

    /**
     * Memverifikasi kode OTP
     */
    fun verifyOTP(otp: String): Boolean {
        Log.d(TAG, "Verifying OTP: $otp")
        val secret = getSecret()

        if (secret == null) {
            Log.e(TAG, "Secret key tidak ditemukan")
            return false
        }

        try {
            val currentTimeMillis = System.currentTimeMillis()
            val timeCounter = currentTimeMillis / 1000 / TIME_STEP

            Log.d(TAG, "Current time: ${currentTimeMillis}ms, Counter: $timeCounter")

            // ===== METODE 2: Implementasi RFC 6238 =====
            for (i in -2..2) {
                val actualCounter = timeCounter + i
                val rfcOtp = generateRFC6238TOTP(secret, actualCounter)

                if (rfcOtp == otp) {
                    Log.d(TAG, "OTP valid dengan RFC 6238")
                    return true
                }
            }

            // ===== METODE 3: Implementasi Google Authenticator =====
            for (i in -2..2) {
                val actualCounter = timeCounter + i
                val googleAuthOtp = generateGoogleAuthenticatorOTP(secret, actualCounter)

                if (googleAuthOtp == otp) {
                    Log.d(TAG, "OTP valid dengan Google Authenticator")
                    return true
                }
            }

            return false
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memverifikasi OTP: ${e.message}", e)
            return false
        }
    }

    /**
     * Menghasilkan TOTP sesuai RFC 6238
     */
    private fun generateRFC6238TOTP(secret: String, time: Long): String {
        try {
            val cleanSecret = secret.replace(" ", "").uppercase()

            val base32 = Base32()
            val key = base32.decode(cleanSecret)

            val data = ByteArray(8)
            var value = time

            for (i in 7 downTo 0) {
                data[i] = (value and 0xff).toByte()
                value = value shr 8
            }

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(data)

            val offset = (hash[hash.size - 1] and 0xf).toInt()

            var binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                    ((hash[offset + 1].toInt() and 0xff) shl 16) or
                    ((hash[offset + 2].toInt() and 0xff) shl 8) or
                    (hash[offset + 3].toInt() and 0xff)

            val otp = binary % Math.pow(10.0, OTP_DIGITS.toDouble()).toInt()

            return String.format("%0${OTP_DIGITS}d", otp)
        } catch (e: Exception) {
            Log.e(TAG, "RFC TOTP generation failed: ${e.message}", e)
            return ""
        }
    }

    /**
     * Implementasi yang paling mendekati Google Authenticator
     */
    private fun generateGoogleAuthenticatorOTP(secret: String, time: Long): String {
        try {
            val cleanSecret = secret.replace(" ", "").uppercase()

            val base32 = Base32()
            val key = base32.decode(cleanSecret)

            val data = ByteArray(8)
            var value = time

            for (i in 7 downTo 0) {
                data[i] = (value and 0xff).toByte()
                value = value shr 8
            }

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(key, "HmacSHA1"))
            val hash = mac.doFinal(data)

            val offset = (hash[hash.size - 1] and 0xf).toInt()

            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                    ((hash[offset + 1].toInt() and 0xff) shl 16) or
                    ((hash[offset + 2].toInt() and 0xff) shl 8) or
                    (hash[offset + 3].toInt() and 0xff)

            val otp = binary % 1000000

            return String.format("%06d", otp)
        } catch (e: Exception) {
            Log.e(TAG, "Google Authenticator TOTP generation failed: ${e.message}", e)
            return ""
        }
    }
}