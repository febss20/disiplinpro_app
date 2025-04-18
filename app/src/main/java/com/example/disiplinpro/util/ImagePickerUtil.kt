package com.example.disiplinpro.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * A utility composable for handling image picking functionality
 * @param context The context used for image picking
 * @param onImagePicked Callback function when an image is selected
 * @return ImagePicker instance to launch the picker
 */
@Composable
fun rememberImagePicker(
    context: Context,
    onImagePicked: (Uri) -> Unit
): ImagePicker {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImagePicked(uri)
        } else {
            // User cancelled the picker without selecting an image
            Toast.makeText(context, "Pemilihan gambar dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    return remember(launcher) {
        ImagePicker(launcher, context)
    }
}

/**
 * ImagePicker class that launches the gallery picker
 * @param launcher The activity result launcher to use
 * @param context The context to check permissions
 */
class ImagePicker(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
    private val context: Context
) {
    /**
     * Launch the image picker to select an image
     * Checks for permissions first
     */
    fun pickImage() {
        if (PermissionUtil.hasStoragePermission(context)) {
            launchPicker()
        } else {
            Toast.makeText(
                context,
                "Izin akses galeri dibutuhkan untuk memilih gambar",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Actually launch the image picker
     */
    private fun launchPicker() {
        launcher.launch("image/*")
    }
}