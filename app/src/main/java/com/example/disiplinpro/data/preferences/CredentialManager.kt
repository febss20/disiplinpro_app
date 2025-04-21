package com.example.disiplinpro.data.preferences

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Kelas untuk mengelola kredensial (username dan password) secara aman menggunakan Android Keystore
 */
class CredentialManager(private val context: Context) {

    companion object {
        private const val TAG = "CredentialManager"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_ALIAS = "DisiplinProCredentialKey"
        private const val IV_SEPARATOR = "]"

        // Nama SharedPreferences untuk informasi login yang terenkripsi
        private const val CREDENTIAL_PREFS = "secure_credential_prefs"
        private const val EMAIL_KEY = "encrypted_email"
        private const val PASSWORD_KEY = "encrypted_password"

        // Flag untuk menunjukkan apakah kredensial tersimpan
        private const val HAS_SAVED_CREDENTIALS = "has_saved_credentials"
    }

    /**
     * Menyimpan kredensial secara aman
     */
    fun saveCredentials(email: String, password: String): Boolean {
        return try {
            val emailEncrypted = encrypt(email)
            val passwordEncrypted = encrypt(password)

            if (emailEncrypted != null && passwordEncrypted != null) {
                val prefs = context.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(EMAIL_KEY, emailEncrypted)
                    .putString(PASSWORD_KEY, passwordEncrypted)
                    .putBoolean(HAS_SAVED_CREDENTIALS, true)
                    .apply()
                Log.d(TAG, "Kredensial berhasil disimpan")
                true
            } else {
                Log.e(TAG, "Gagal mengenkripsi kredensial")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyimpan kredensial: ${e.message}")
            false
        }
    }

    /**
     * Mengambil email tersimpan
     */
    fun getSavedEmail(): String? {
        return try {
            val prefs = context.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
            val encryptedEmail = prefs.getString(EMAIL_KEY, null) ?: return null
            decrypt(encryptedEmail)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mendapatkan email: ${e.message}")
            null
        }
    }

    /**
     * Mengambil password tersimpan
     */
    fun getSavedPassword(): String? {
        return try {
            val prefs = context.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
            val encryptedPassword = prefs.getString(PASSWORD_KEY, null) ?: return null
            decrypt(encryptedPassword)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mendapatkan password: ${e.message}")
            null
        }
    }

    /**
     * Memeriksa apakah ada kredensial tersimpan
     */
    fun hasCredentials(): Boolean {
        val prefs = context.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(HAS_SAVED_CREDENTIALS, false)
    }

    /**
     * Menghapus semua kredensial tersimpan
     */
    fun clearCredentials() {
        val prefs = context.getSharedPreferences(CREDENTIAL_PREFS, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.d(TAG, "Semua kredensial berhasil dihapus")
    }

    /**
     * Membuat atau mendapatkan kunci enkripsi dari Android Keystore
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        }

        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Enkripsi data menggunakan AES/GCM/NoPadding
     */
    private fun encrypt(text: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charset.defaultCharset()))

            // Simpan IV bersama dengan teks terenkripsi untuk digunakan saat dekripsi
            val ivAndEncryptedText = Base64.encodeToString(iv, Base64.DEFAULT) +
                    IV_SEPARATOR +
                    Base64.encodeToString(encryptedBytes, Base64.DEFAULT)

            ivAndEncryptedText
        } catch (e: Exception) {
            Log.e(TAG, "Enkripsi gagal: ${e.message}")
            null
        }
    }

    /**
     * Dekripsi data menggunakan AES/GCM/NoPadding
     */
    private fun decrypt(encryptedText: String): String? {
        return try {
            val split = encryptedText.split(IV_SEPARATOR)
            if (split.size != 2) return null

            val iv = Base64.decode(split[0], Base64.DEFAULT)
            val encryptedBytes = Base64.decode(split[1], Base64.DEFAULT)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charset.defaultCharset())
        } catch (e: Exception) {
            Log.e(TAG, "Dekripsi gagal: ${e.message}")
            null
        }
    }
}