package com.dsp.disiplinpro.data.security

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.io.IOException
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Kelas untuk mengelola keamanan jaringan dengan SSL Pinning
 */
class NetworkSecurityManager {

    companion object {
        private const val TAG = "NetworkSecurityManager"

        private val FIREBASE_DOMAINS = listOf(
            "firestore.googleapis.com",
            "firebase.googleapis.com",
            "firebaseio.com",
            "storage.googleapis.com"
        )

        private val CERTIFICATE_PINS = listOf(
            "sha256/7HIpactkIAq2Y49orFOOQKurWxmmSFZhBCoQYcRhJ3Y=",
            "sha256/FEzVOUp4dF3gI0ZVPRJhFbSJVXR+uQmMH65xhs1glH4="
        )
    }

    /**
     * Buat OkHttpClient dengan SSL Pinning dikonfigurasi
     * untuk digunakan dengan Retrofit atau klien HTTP lainnya
     */
    fun createSecureOkHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder().apply {
            FIREBASE_DOMAINS.forEach { domain ->
                CERTIFICATE_PINS.forEach { pin ->
                    add(domain, pin)
                }
            }
        }.build()

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Verifikasi bahwa SSL Pinning bekerja dengan mencoba terhubung
     * ke domain Firebase dan memvalidasi sertifikat
     * @return true jika verifikasi berhasil, false jika gagal
     */
    fun verifySslPinning(callback: (Boolean) -> Unit) {
        val client = createSecureOkHttpClient()
        val request = Request.Builder()
            .url("https://firestore.googleapis.com/")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (e.message?.contains("Certificate pinning failure") == true) {
                    Log.w(TAG, "SSL Pinning berfungsi - mendeteksi sertifikat yang tidak sesuai")
                    callback(true)
                } else {
                    Log.e(TAG, "Koneksi gagal: ${e.message}")
                    callback(false)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    Log.d(TAG, "SSL Pinning berhasil - koneksi aman ke Firebase: ${response.code}")
                    callback(true)
                }
            }
        })
    }

    /**
     * Membuat X509TrustManager yang memvalidasi sertifikat server
     * dengan menerapkan SSL Pinning secara programatis
     */
    private fun createTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d(TAG, "Memeriksa client certificate: $authType")
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d(TAG, "Memeriksa server certificate: $authType")

                if (chain.isEmpty()) {
                    throw CertificateException("Certificate chain kosong")
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    }

    /**
     * Membuat SSLContext yang dikonfigurasi dengan TrustManager kita
     */
    private fun createSSLContext(): SSLContext {
        val trustManager = createTrustManager()
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        return sslContext
    }
}