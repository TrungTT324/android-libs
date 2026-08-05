package hdisoft.app.core.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

object NetworkUtils {

    // RFC1918 private ranges: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16.
    // Note the 172.16.0.0/12 block spans second octet 16-31 (172.17.x.x, 172.20.x.x,
    // 172.24.x.x, etc. are just as valid as 172.16./172.31.) — a plain string-prefix
    // check on only those two boundary values would silently miss the other 14 and
    // make the local IP (and therefore the LAN scan subnet) resolve to null on those
    // networks, which are common for Docker/VM host networks and some routers.
    private fun isPrivateIPv4(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        val octets = parts.map { it.toIntOrNull() ?: return false }
        if (octets.any { it !in 0..255 }) return false
        return octets[0] == 10 ||
            (octets[0] == 172 && octets[1] in 16..31) ||
            (octets[0] == 192 && octets[1] == 168)
    }

    fun getLocalSubnet(context: Context): String? {
        val localIp = getLocalIpAddress(context) ?: return null
        val lastDotIndex = localIp.lastIndexOf('.')
        if (lastDotIndex == -1) return null
        return localIp.substring(0, lastDotIndex + 1)
    }

    fun getLocalIpAddress(context: Context): String? {
        // First try: via Network Interfaces (cleaner, permission-free, and works on all Android versions)
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            
            // Pass order matters: prefer "wlan" strictly over "eth" over everything else.
            // On the Android emulator both eth0 (fixed NAT gateway-side IP, always 10.0.2.15
            // on every AVD instance) and wlan0 (the real per-device virtual LAN IP used for
            // actual peer-to-peer discovery) can be present together; grouping them into one
            // pass returns whichever the OS happens to enumerate first, which silently picks
            // the useless identical-on-every-device eth0 IP instead of wlan0.
            for (preferredNameFragment in listOf("wlan", "eth")) {
                for (networkInterface in interfaces) {
                    val name = networkInterface.name.lowercase(Locale.ROOT)
                    if (name.contains(preferredNameFragment)) {
                        val addresses = Collections.list(networkInterface.inetAddresses)
                        for (address in addresses) {
                            if (!address.isLoopbackAddress) {
                                val ip = address.hostAddress ?: continue
                                if (ip.indexOf(':') < 0 && isPrivateIPv4(ip)) { // IPv4, private range only
                                    return ip
                                }
                            }
                        }
                    }
                }
            }

            // Final fallback: any other interface not already covered above.
            for (networkInterface in interfaces) {
                val name = networkInterface.name.lowercase(Locale.ROOT)
                if (!name.contains("wlan") && !name.contains("eth")) {
                    val addresses = Collections.list(networkInterface.inetAddresses)
                    for (address in addresses) {
                        if (!address.isLoopbackAddress) {
                            val ip = address.hostAddress ?: continue
                            if (ip.indexOf(':') < 0 && isPrivateIPv4(ip)) { // IPv4, private range only
                                return ip
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        // Second try (Fallback): via WifiManager (requires location permission on Android 10+, so we catch silently)
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val ipAddress = wifiManager.connectionInfo.ipAddress
            if (ipAddress != 0) {
                val ipString = String.format(
                    Locale.US,
                    "%d.%d.%d.%d",
                    (ipAddress and 0xff),
                    (ipAddress shr 8 and 0xff),
                    (ipAddress shr 16 and 0xff),
                    (ipAddress shr 24 and 0xff)
                )
                if (ipString != "0.0.0.0") return ipString
            }
        } catch (e: Exception) {
            // Log quietly to avoid spamming system.err when location permission is not granted
        }

        return null
    }
}
