package hdisoft.app.core.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Checks if a single permission is granted.
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if all specified permissions are granted.
     */
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (!hasPermission(context, permission)) {
                return false
            }
        }
        return true
    }

    /**
     * Helper to check Record Audio permission.
     */
    fun hasAudioPermission(context: Context): Boolean {
        return hasPermission(context, android.Manifest.permission.RECORD_AUDIO)
    }
}
