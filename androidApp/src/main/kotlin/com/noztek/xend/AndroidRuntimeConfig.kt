package com.noztek.xend

import android.os.Build

object AndroidRuntimeConfig {
    fun apiBaseUrl(): String {
        return if (isEmulator()) {
            BuildConfig.EMULATOR_API_BASE_URL
        } else {
            BuildConfig.DEVICE_API_BASE_URL
        }
    }

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("sdk")
    }
}
