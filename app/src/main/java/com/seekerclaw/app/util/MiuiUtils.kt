package com.seekerclaw.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

object MiuiUtils {
    /**
     * Detects if the current device is running MIUI.
     */
    fun isMiui(): Boolean {
        return !getSystemProperty("ro.miui.ui.version.name").isNullOrBlank()
    }

    /**
     * Opens the MIUI Auto-start management screen.
     */
    fun openAutoStartSettings(context: Context) {
        try {
            val intent = Intent()
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent()
                intent.action = "miui.intent.action.OP_AUTO_START"
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Fallback to app info
                openAppInfo(context)
            }
        }
    }

    /**
     * Opens the MIUI-specific battery optimization settings for the app.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent("miui.intent.action.POWER_HIDE_MODE_APP_DETAILS")
            intent.setClassName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")
            intent.putExtra("package_name", context.packageName)
            intent.putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager).toString())
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic battery optimization
            try {
                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            } catch (e2: Exception) {
                openAppInfo(context)
            }
        }
    }

    private fun openAppInfo(context: Context) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }

    @SuppressLint("PrivateApi")
    private fun getSystemProperty(key: String): String? {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            get.invoke(c, key) as String
        } catch (e: Exception) {
            null
        }
    }
}
