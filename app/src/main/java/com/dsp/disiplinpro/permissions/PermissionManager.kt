package com.dsp.disiplinpro.permissions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PermissionManager(private val activity: FragmentActivity) {
    private val requestPermissionLauncher: ActivityResultLauncher<String> = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Izin notifikasi diberikan")
        } else {
            Toast.makeText(activity, "Izin notifikasi diperlukan untuk pengingat!", Toast.LENGTH_LONG).show()
        }
    }

    private val requestAlarmPermissionLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        Log.d(TAG, "Hasil permintaan izin alarm: ${it.resultCode}")
    }

    fun requestAllPermissions() {
        requestNotificationPermission()
        requestBatteryOptimizationExemption()
        requestExactAlarmPermission()
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = activity.getSystemService(Activity.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.data = Uri.parse("package:${activity.packageName}")
                requestAlarmPermissionLauncher.launch(intent)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Izin notifikasi sudah diberikan")
                }
                activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(
                        activity,
                        "DisiplinPro memerlukan izin notifikasi untuk mengingatkan Anda tentang tugas dan jadwal",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = activity.packageName
            val pm = activity.getSystemService(Activity.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                activity.startActivity(intent)
            }
        }
    }

    companion object {
        private const val TAG = "PermissionManager"
    }
}