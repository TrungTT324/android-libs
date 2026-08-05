package hdisoft.app.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardUtils {

    /**
     * Copies text to clipboard and optionally shows a Toast confirmation.
     */
    fun copyToClipboard(
        context: Context,
        label: String,
        text: String,
        showToast: Boolean = true,
        toastMessage: String? = null
    ): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return false
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            if (showToast) {
                val msg = toastMessage ?: "Đã sao chép vào bộ nhớ tạm"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads text content from clipboard.
     */
    fun readFromClipboard(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                ?: return null
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
