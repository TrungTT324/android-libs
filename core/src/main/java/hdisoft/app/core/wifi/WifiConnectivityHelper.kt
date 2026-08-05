package hdisoft.app.core.wifi

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import hdisoft.app.core.utils.NetworkUtils

/**
 * Single place for "does this device have usable Wi-Fi right now" + "try to get it back
 * on" - pulled out of [NetworkUtils] into its own class so any module can check/react to
 * Wi-Fi state without pulling in [NetworkUtils]'s unrelated IP/subnet-parsing concerns.
 */
object WifiConnectivityHelper {

    /** True when the Wi-Fi radio itself is on (says nothing about being connected). */
    fun isEnabled(context: Context): Boolean {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            false
        }
    }

    /**
     * True only when Wi-Fi is both on AND actually associated with a network (has been
     * assigned an IP) - radio-on alone isn't enough, since a flaky adapter or a Wi-Fi
     * that's "on" but not actually connected never gets assigned an IP either.
     */
    fun isConnected(context: Context): Boolean {
        return isEnabled(context) && NetworkUtils.getLocalIpAddress(context) != null
    }

    /**
     * Best-effort turn-Wi-Fi-back-on: tries to toggle it directly first (still works on
     * pre-Android-10 targets), then falls back to opening the system Wi-Fi quick-settings
     * panel - apps can no longer flip Wi-Fi on programmatically without user action from
     * Android 10 (API 29) onward.
     */
    fun ensureEnabled(context: Context): Boolean {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                @Suppress("DEPRECATION")
                val turnedOn = try {
                    wifiManager.isWifiEnabled = true
                    wifiManager.isWifiEnabled
                } catch (e: Exception) {
                    false
                }

                if (!turnedOn && !wifiManager.isWifiEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
                Toast.makeText(context, "Wi-Fi đang tắt — Tự động bật Wi-Fi...", Toast.LENGTH_SHORT).show()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
