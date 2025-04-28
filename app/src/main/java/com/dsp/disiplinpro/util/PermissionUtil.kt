package com.dsp.disiplinpro.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Utility class to check and request storage permissions
 */
object PermissionUtil {
    /**
     * Checks if the app has storage permission based on the Android version
     * @param context The context to check permissions
     * @return True if permission is granted, false otherwise
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns the appropriate storage permission based on the Android version
     * @return The permission string
     */
    fun getStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}

/**
 * Composable function to handle storage permission requests
 * @param context The context for permission checks
 * @param onPermissionGranted Callback when permission is granted
 * @param onPermissionDenied Callback when permission is denied
 */
@Composable
fun RequestStoragePermission(
    context: Context,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    val hasPermission = remember(context) {
        PermissionUtil.hasStoragePermission(context)
    }

    if (!hasPermission) {
        SideEffect {
            permissionLauncher.launch(PermissionUtil.getStoragePermission())
        }
    } else {
        onPermissionGranted()
    }
}