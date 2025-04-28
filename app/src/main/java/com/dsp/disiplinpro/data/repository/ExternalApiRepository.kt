package com.dsp.disiplinpro.data.repository

import android.util.Log
import com.dsp.disiplinpro.data.network.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Repository untuk mengakses API eksternal dengan SSL Pinning
 * Ini adalah contoh bagaimana menggunakan NetworkHelper untuk API selain Firebase
 */
class ExternalApiRepository {

    companion object {
        private const val TAG = "ExternalApiRepository"
    }

    /**
     * Contoh fungsi untuk mendapatkan data dari API eksternal dengan SSL Pinning
     * @param endpoint URL API endpoint
     * @return Pair<Boolean, String?> - sukses/gagal dan data/error message
     */
    suspend fun fetchExternalData(endpoint: String): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val call = NetworkHelper.secureGet(endpoint) { success, data, error ->
                        if (success && data != null) {
                            Log.d(TAG, "Data berhasil diambil dari endpoint: $endpoint")
                            continuation.resume(Pair(true, data))
                        } else {
                            Log.e(TAG, "Gagal mengambil data: $error")
                            continuation.resume(Pair(false, error))
                        }
                    }

                    continuation.invokeOnCancellation {
                        call.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saat memanggil API: ${e.message}")
                    continuation.resume(Pair(false, "Error: ${e.message}"))
                }
            }
        }
    }

    /**
     * Contoh fungsi untuk mengambil data cuaca
     * @param cityId ID kota
     * @return data cuaca atau pesan error
     */
    suspend fun getWeatherData(cityId: String): Pair<Boolean, String?> {
        // Contoh URL, ganti dengan URL API yang sebenarnya
        val apiUrl = "https://api.example.com/weather?city=$cityId"
        return fetchExternalData(apiUrl)
    }

    /**
     * Contoh fungsi untuk mengambil berita pendidikan
     * @return data berita atau pesan error
     */
    suspend fun getEducationNews(): Pair<Boolean, String?> {
        // Contoh URL, ganti dengan URL API yang sebenarnya
        val apiUrl = "https://api.example.com/news/education"
        return fetchExternalData(apiUrl)
    }
}