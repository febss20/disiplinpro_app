package com.dsp.disiplinpro.data.repository

import android.content.Context
import android.net.Uri
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.HttpMethod
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.dsp.disiplinpro.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class S3Repository(private val context: Context) {

    companion object {
        private const val BUCKET_NAME = "disiplinpro-profile-photos"
        private val REGION = Regions.AP_SOUTHEAST_1
    }

    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(BuildConfig.AWS_ACCESS_KEY, BuildConfig.AWS_SECRET_KEY)
        val client = AmazonS3Client(credentials)
        client.setRegion(Region.getRegion(REGION))
        client
    }

    private val transferUtility: TransferUtility by lazy {
        TransferNetworkLossHandler.getInstance(context)
        TransferUtility.builder()
            .context(context)
            .s3Client(s3Client)
            .defaultBucket(BUCKET_NAME)
            .build()
    }

    suspend fun uploadProfilePhoto(uri: Uri, userId: String): String = withContext(Dispatchers.IO) {
        val file = createTempFileFromUri(context, uri)
        val objectKey = "profile_photos/$userId/${UUID.randomUUID()}"

        return@withContext suspendCancellableCoroutine { continuation ->
            val observer = transferUtility.upload(objectKey, file)
            val transferId = observer.id

            observer.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (state == TransferState.COMPLETED) {
                        try {
                            // Generate a pre-signed URL with expiration of 7 days
                            val expirationDate = Date()
                            expirationDate.time = expirationDate.time + TimeUnit.DAYS.toMillis(7)

                            val presignedUrlRequest = GeneratePresignedUrlRequest(BUCKET_NAME, objectKey)
                                .withMethod(HttpMethod.GET)
                                .withExpiration(expirationDate)

                            val presignedUrl = s3Client.generatePresignedUrl(presignedUrlRequest).toString()

                            val resultMap = mapOf(
                                "url" to presignedUrl,
                                "objectKey" to objectKey,
                                "expiration" to expirationDate.time
                            )

                            continuation.resume(presignedUrl)
                            file.delete()
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                            file.delete()
                        }
                    } else if (state == TransferState.FAILED || state == TransferState.CANCELED) {
                        continuation.resumeWithException(Exception("Upload failed"))
                        file.delete()
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {

                }

                override fun onError(id: Int, ex: Exception) {
                    continuation.resumeWithException(ex)
                    file.delete()
                }
            })

            continuation.invokeOnCancellation {
                transferUtility.cancel(transferId)
                file.delete()
            }
        }
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)

        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    suspend fun deleteProfilePhoto(fileUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val objectKeyPattern = ".*profile_photos/[^?]+".toRegex()
            val match = objectKeyPattern.find(fileUrl)
            val objectKey = match?.value?.substringAfter("$BUCKET_NAME/") ?: return@withContext false

            s3Client.deleteObject(BUCKET_NAME, objectKey)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Add a method to refresh pre-signed URLs that are about to expire
    suspend fun refreshProfilePhotoUrl(objectKey: String): String = withContext(Dispatchers.IO) {
        val expirationDate = Date()
        expirationDate.time = expirationDate.time + TimeUnit.DAYS.toMillis(7)

        val presignedUrlRequest = GeneratePresignedUrlRequest(BUCKET_NAME, objectKey)
            .withMethod(HttpMethod.GET)
            .withExpiration(expirationDate)

        return@withContext s3Client.generatePresignedUrl(presignedUrlRequest).toString()
    }
}