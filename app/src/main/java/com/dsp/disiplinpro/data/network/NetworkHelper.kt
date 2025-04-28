package com.dsp.disiplinpro.data.network

import android.util.Log
import com.dsp.disiplinpro.DisiplinProApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException

/**
 * Helper class untuk melakukan HTTP requests dengan SSL Pinning
 * Gunakan class ini untuk melakukan panggilan jaringan non-Firebase
 */
class NetworkHelper {

    companion object {
        private const val TAG = "NetworkHelper"

        /**
         * Mendapatkan OkHttpClient yang sudah dikonfigurasi dengan SSL Pinning
         */
        fun getSecureClient(): OkHttpClient {
            return DisiplinProApplication.secureHttpClient
        }

        /**
         * Melakukan HTTP GET request dengan SSL Pinning
         * @param url URL untuk request
         * @param callback Callback untuk hasil
         * @return Call object yang dapat dibatalkan
         */
        fun secureGet(url: String, callback: (success: Boolean, data: String?, error: String?) -> Unit): Call {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val call = getSecureClient().newCall(request)
                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "Request failed: ${e.message}")
                        callback(false, null, "Network error: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string() ?: ""
                            callback(true, responseData, null)
                        } else {
                            val errorMsg = "HTTP error: ${response.code}"
                            Log.e(TAG, errorMsg)
                            callback(false, null, errorMsg)
                        }
                        response.close()
                    }
                })

                return call
            } catch (e: Exception) {
                Log.e(TAG, "Error creating request: ${e.message}")
                throw e
            }
        }

        /**
         * Contoh penggunaan untuk panggilan REST API eksternal
         */
        fun exampleApiCall(apiEndpoint: String, callback: (success: Boolean, data: String?, error: String?) -> Unit) {
            secureGet(apiEndpoint) { success, data, error ->
                if (success && data != null) {
                    try {
                        callback(true, data, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing API response: ${e.message}")
                        callback(false, null, "Error parsing response: ${e.message}")
                    }
                } else {
                    callback(false, null, error)
                }
            }
        }
    }
}