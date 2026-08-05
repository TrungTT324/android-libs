package hdisoft.app.core.utils

import java.util.Locale

object FormatUtils {

    /**
     * Formats bytes to human-readable string (B, KB, MB, GB).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
    }

    /**
     * Formats time duration in milliseconds into "mm:ss" or "hh:mm:ss".
     */
    fun formatDurationMs(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats BuildNo (e.g. 202607251140) to readable string (e.g. "25 @ 11:40").
     */
    fun formatBuildNo(buildNo: Long): String {
        val buildNoStr = buildNo.toString()
        if (buildNoStr.length == 12) {
            val day = buildNoStr.substring(6, 8)
            val hour = buildNoStr.substring(8, 10)
            val minute = buildNoStr.substring(10, 12)
            return "$day @ $hour:$minute"
        }
        return buildNoStr
    }
}
