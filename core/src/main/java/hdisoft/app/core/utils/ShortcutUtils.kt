package hdisoft.app.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log

object ShortcutUtils {
    private const val TAG = "ShortcutUtils"
    private const val PREF_NAME = "app_shortcut_prefs"
    private const val KEY_SHORTCUT_CREATED = "shortcut_created"

    /**
     * Tự động tạo Phím tắt (Shortcut) trên Màn hình chính (Home screen) khi ứng dụng được mở / sau khi cài đặt.
     * Chỉ tạo 1 lần duy nhất bằng cách lưu trạng thái vào SharedPreferences.
     *
     * @param context Application context hoặc Activity context.
     * @param targetClass Class Activity chính của ứng dụng (ví dụ MainActivity::class.java).
     * @param labelName Tên hiển thị phím tắt (nếu null sẽ lấy label của ứng dụng).
     * @param iconResId Icon id đại diện cho phím tắt (nếu null sẽ lấy icon ứng dụng).
     * @param forceRecreate Nếu true sẽ bỏ qua kiểm tra đã tạo trước đó.
     */
    fun createHomeScreenShortcut(
        context: Context,
        targetClass: Class<*>,
        labelName: String? = null,
        iconResId: Int? = null,
        forceRecreate: Boolean = false
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!forceRecreate && prefs.getBoolean(KEY_SHORTCUT_CREATED, false)) {
            Log.d(TAG, "Shortcut already created or requested previously.")
            return
        }

        val appLabel = labelName ?: run {
            val appInfo = context.applicationInfo
            val stringId = appInfo.labelRes
            if (stringId != 0) {
                context.getString(stringId)
            } else {
                appInfo.nonLocalizedLabel?.toString() ?: context.packageName
            }
        }

        val appIcon = iconResId ?: context.applicationInfo.icon

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val shortcutManager = context.getSystemService(ShortcutManager::class.java)
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                    val shortcutIntent = Intent(context, targetClass).apply {
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }

                    val pinShortcutInfo = ShortcutInfo.Builder(context, "home_shortcut_${context.packageName}")
                        .setShortLabel(appLabel)
                        .setLongLabel(appLabel)
                        .setIcon(Icon.createWithResource(context, appIcon))
                        .setIntent(shortcutIntent)
                        .build()

                    val success = shortcutManager.requestPinShortcut(pinShortcutInfo, null)
                    Log.i(TAG, "Requested pin shortcut for '$appLabel' (success: $success)")
                    prefs.edit().putBoolean(KEY_SHORTCUT_CREATED, true).apply()
                } else {
                    Log.w(TAG, "Pin shortcut is not supported by default launcher.")
                }
            } else {
                // Legacy shortcut broadcast for Android 7.1 and lower
                val shortcutIntent = Intent(context, targetClass).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val addIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, appLabel)
                    putExtra(
                        Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(context, appIcon)
                    )
                    putExtra("duplicate", false)
                }
                context.sendBroadcast(addIntent)
                Log.i(TAG, "Sent INSTALL_SHORTCUT broadcast for '$appLabel'")
                prefs.edit().putBoolean(KEY_SHORTCUT_CREATED, true).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating home screen shortcut: ${e.message}", e)
        }
    }
}
